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
            queues = "${rabbitmq.exchange.analysis.queue}"
    )
    public void listen(@Payload AnalysisCreatedEvent event) {

        var dto = event.analysisDTO();
        var correlationId = dto.correlationId();

        AnalysisJobEntity job = jobRepository
                .findByCorrelationId(correlationId)
                .orElseThrow(() -> new AnalysisJobNotFoundException(
                        "Job not found: " + correlationId
                ));

//        if (job.getJobStatus() == JobStatus.IN_QUEUE) {
//            log.info("Job {} ja está na fila", correlationId);
//            return;
//        }

        Map<String, AnalysisCreatedEvent> request =
                Map.of("input", event);

        log.debug("Enviando analise: {}", correlationId);

        KatagoErrorMessage response = rest.postForObject(
                postUrl,
                request,
                KatagoErrorMessage.class
        );

        if (response.error() != null
//                || !"IN_QUEUE".equals(response.status())
        ) {
            job.setJobStatus(JobStatus.FAILED);
            throw new KataGoAnalysisRejectedException(response);
        }

        job.setJobStatus(JobStatus.IN_QUEUE);
        jobRepository.save(job);

        log.info("Análise {} aceita pelo KataGo", correlationId);

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
                    "Falha ao notificar chat sobre análise {}",
                    correlationId,
                    e
            );
            throw new ChatNotificationException( "Failed to notify chat: " + job.getChat().getChatId() + " chat about: " + correlationId);

        }
    }
}

// Processamento concluído = channel.basicAck(deliveryTag, false);
// Falha temporária, tentar novamente = basicNack(tag, false, true)
// Falha definitiva, não tentar novamente = basicNack(tag, false, false)
