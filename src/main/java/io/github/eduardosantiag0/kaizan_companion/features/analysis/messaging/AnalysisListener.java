package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import com.rabbitmq.client.Channel;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.analysis.AnalysisJobNotFoundException;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.RunPodClient;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.errors.KatagoErrorMessage;
import io.github.eduardosantiag0.kaizan_companion.utils.MessageFormatter;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.io.IOException;

@Component
@Slf4j
public class AnalysisListener {

    private final String postUrl;
    private final AnalysisJobRepository jobRepository;
    private final NotificationService notificationService;
    private final RunPodClient runPodClient;

    public AnalysisListener(
            @Value("${katago.post.url.sync}") String postUrl,
            AnalysisJobRepository jobRepository,
            NotificationService notificationService, RunPodClient runPodClient
    ) {
        this.postUrl = postUrl;
        this.jobRepository = jobRepository;
        this.notificationService = notificationService;
        this.runPodClient = runPodClient;
    }

    @RabbitListener(
            queues = "${rabbitmq.queue.analysis}",
            ackMode = "MANUAL"
    )
    public void listen(
            @Payload AnalysisCreatedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        log.debug("Chegou no listener!");


//        TODO Checar se já existe essa correlationId

        var dto = event.analysisDTO();
        var correlationId = dto.correlationId();


        AnalysisJobEntity job = jobRepository
                .findByCorrelationId(correlationId)
                .orElseThrow(() -> new AnalysisJobNotFoundException(
                        "Job not found: " + correlationId
                ));

        // Idempotent Consumer
        if (job.getJobStatus() == JobStatus.IN_QUEUE) {
            log.info("Job already in queue {} ", correlationId);
            channel.basicAck(deliveryTag, false);
            return;
        }

        KatagoErrorMessage response = runPodClient.sendEvent(event, postUrl);

        if (response == null) {
            throw new IllegalStateException(
                    "Empty KataGo response"
            );
        }

        job.setJobStatus(JobStatus.IN_QUEUE);
        jobRepository.save(job);

        if (response.error() != null) {
            job.setJobStatus(JobStatus.FAILED);
            jobRepository.save(job);
            log.error("KataGo rejected {}: {}", correlationId, response);
            // Falha definitiva, não tentar novamente
            channel.basicNack(deliveryTag, false, false);
            notificationService.sendErrorMessage(new ReplyWithText(dto.chatId(),
                    MessageFormatter.requestFailed()));
            return;
        }


        try {
            notificationService.sendMessageToChat(
                    new ReplyWithText(
                            dto.chatId(),
                            MessageFormatter.requestsIsInQueue(
                                    correlationId.toString()
                            )
                    )
            );
        } catch (Exception e) {
            log.error(
                    "Failed to notify channel {}",
                    correlationId, e
            );
        }

        // Processamento concluído
        channel.basicAck(deliveryTag, false);


        log.info("Análise {} aceita pelo KataGo", correlationId);

    }

}

//    private void exceptionHandling (Throwable e) throws Throwable {
//        if (e instanceof AnalysisJobNotFoundException) {
//            log.error("Job inexistente: {}", correlationId, e);
//
//            // Falha definitiva -> DLQ, se configurada
//            channel.basicNack(deliveryTag, false, false);
//            throw e;
//        }
//    }

// Processamento concluído = channel.basicAck(deliveryTag, false);
// Falha temporária, tentar novamente = basicNack(tag, false, true)
// Falha definitiva, não tentar novamente = basicNack(tag, false, false)

// RunPod offline
// Nao conseguir notificar o canal
// Job inexistente
// Falha inesperada

//Erro	Classificação provável	Estratégia
//HTTP 400 — Bad Request	Definitiva	Não tentar novamente
//HTTP 401 — Unauthorized	Configuração/autenticação	Interromper e investigar
//HTTP 429 — Too Many Requests	Temporária	Retry com backoff
//HTTP 503 — Service Unavailable	Temporária	Retry com backoff
