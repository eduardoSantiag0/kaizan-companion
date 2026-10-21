package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import com.rabbitmq.client.Channel;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.analysis.AnalysisJobNotFoundException;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.analysis.KataGoAnalysisRejectedException;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.telegram.ChatNotificationException;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.AnalysisAggregator;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.errors.KatagoErrorMessage;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.MessageFormatter;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.replies.ReplyWithText;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
@Component
@Slf4j
public class AnalysisListener {

    private final String postUrl;
    private final RestTemplate rest = new RestTemplate();
    private final AnalysisJobRepository jobRepository;
    private final NotificationService notificationService;

    public AnalysisListener(
            @Value("${katago.post.url.sync}") String postUrl,
            AnalysisJobRepository jobRepository,
            NotificationService notificationService
    ) {
        this.postUrl = postUrl;
        this.jobRepository = jobRepository;
        this.notificationService = notificationService;
    }

    @RabbitListener(
            queues = "${rabbitmq.exchange.analysis.queue}",
            ackMode = "MANUAL"
    )
    public void listen(
            @Payload AnalysisCreatedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        var dto = event.analysisDTO();
        var correlationId = dto.correlationId();

        try {
            AnalysisJobEntity job = jobRepository
                    .findByCorrelationId(correlationId)
                    .orElseThrow(() -> new AnalysisJobNotFoundException(
                            "Job not found: " + correlationId
                    ));

            if (job.getJobStatus() == JobStatus.IN_QUEUE) {
                log.info("Job already in queue {} ", correlationId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            Map<String, AnalysisCreatedEvent> request =
                    Map.of("input", event);

            KatagoErrorMessage response = rest.postForObject(
                    postUrl,
                    request,
                    KatagoErrorMessage.class
            );

            if (response == null) {
                throw new IllegalStateException(
                        "Empty KataGo response"
                );
            }

            if (response.error() != null) {
                job.setJobStatus(JobStatus.FAILED);
                jobRepository.save(job);
                log.error("KataGo rejected {}: {}", correlationId, response);
                // Falha definitiva, não tentar novamente
                channel.basicNack(deliveryTag, false, false);
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
            }

            // Processamento concluído
            channel.basicAck(deliveryTag, false);

            log.info("Análise {} aceita pelo KataGo", correlationId);

        } catch (AnalysisJobNotFoundException e) {
            log.error("Job inexistente: {}", correlationId, e);

            // Falha definitiva -> DLQ, se configurada
            channel.basicNack(deliveryTag, false, false);

        } catch (RestClientException e) {
            log.error("Falha HTTP ao enviar análise {}", correlationId, e);

            // Falha temporária, tentar novamente
            channel.basicNack(deliveryTag, false, true);

        } catch (RuntimeException e) {
            log.error("Falha inesperada na análise {}", correlationId, e);

            channel.basicNack(deliveryTag, false, true);
        }
    }
}

// Processamento concluído = channel.basicAck(deliveryTag, false);
// Falha temporária, tentar novamente = basicNack(tag, false, true)
// Falha definitiva, não tentar novamente = basicNack(tag, false, false)
