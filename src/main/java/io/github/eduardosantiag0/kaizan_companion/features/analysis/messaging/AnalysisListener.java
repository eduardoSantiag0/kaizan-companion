package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import com.rabbitmq.client.Channel;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.exceptions.analysis.AnalysisJobNotFoundException;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.JobClaimService;
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

import java.io.IOException;

@Component
@Slf4j
public class AnalysisListener {

    private final String postUrl;
    private final AnalysisJobRepository jobRepository;
    private final NotificationService notificationService;
    private final RunPodClient runPodClient;
    private final JobClaimService claimService;

    public AnalysisListener(
            @Value("${katago.post.url.sync}") String postUrl,
            AnalysisJobRepository jobRepository,
            NotificationService notificationService, RunPodClient runPodClient, JobClaimService claimService
    ) {
        this.postUrl = postUrl;
        this.jobRepository = jobRepository;
        this.notificationService = notificationService;
        this.runPodClient = runPodClient;
        this.claimService = claimService;
    }

    @RabbitListener(
            id = "analysisListenerContainer",
            queues = "${rabbitmq.queue.analysis}",
            ackMode = "MANUAL"
    )
    public void listen(
            @Payload AnalysisCreatedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        log.debug("Chegou no listener!");

        var dto = event.analysisDTO();
        var correlationId = dto.correlationId();

        AnalysisJobEntity job = jobRepository
                .findByCorrelationId(correlationId)
                .orElseThrow(() -> new AnalysisJobNotFoundException(
                        "Job not found: " + correlationId
                ));


        // Claim atomico: jobId + status
        switch (job.getJobStatus()) {
            case ANALYSIS_REQUESTED -> {
                // Uma thread acabou de pegar o claim
                if (!claimService.claim(job)) {
                    log.info("Perdeu a corrida pelo claim do job {}, descartando entrega duplicada",
                            correlationId);
                    channel.basicAck(deliveryTag, false);
                    return;
                }
            }
            case SUBMITTING -> {
                // Alguem já pegou o claim
                log.info("Job {} já em processamento (lease até {}), descartando entrega duplicada",
                        correlationId, job.getLeaseUntil());
                channel.basicAck(deliveryTag, false);
                return;
            }
            default -> {
                log.info("Job {} em estado terminal ({}), descartando entrega duplicada",
                        correlationId, job.getJobStatus());
                channel.basicAck(deliveryTag, false);
                return;
            }
        }

        KatagoErrorMessage response = runPodClient.sendEvent(event, postUrl,
                channel, deliveryTag);


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

        job.setJobStatus(JobStatus.IN_QUEUE);
        jobRepository.save(job);

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
            throw e;
        }

        channel.basicAck(deliveryTag, false);

        log.info("Análise {} aceita pelo KataGo", correlationId);

    }

}

// Processamento concluído = channel.basicAck(deliveryTag, false);
// Falha temporária, tentar novamente = basicNack(tag, false, true)
// Falha definitiva, não tentar novamente = basicNack(tag, false, false)

