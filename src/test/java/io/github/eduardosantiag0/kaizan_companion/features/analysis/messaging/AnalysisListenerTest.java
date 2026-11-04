package io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging;

import com.rabbitmq.client.Channel;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.JobClaimService;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.RunPodClient;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisDTO;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class AnalysisListenerTest {

    @Test
    void listen_mustNotThrow_whenRunPodClientAlreadyHandledTheDeliveryItself() throws Exception {
        AnalysisJobRepository jobRepository = mock(AnalysisJobRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        RunPodClient runPodClient = mock(RunPodClient.class);
        JobClaimService claimService = mock(JobClaimService.class);
        Channel channel = mock(Channel.class);

        UUID correlationId = UUID.randomUUID();
        TelegramChatEntity chat = new TelegramChatEntity(1L, "Tester", "tester", "private", false);
        AnalysisJobEntity job = new AnalysisJobEntity(chat, Instant.now(), correlationId);
        job.setJobStatus(JobStatus.ANALYSIS_REQUESTED);

        when(jobRepository.findByCorrelationId(correlationId)).thenReturn(Optional.of(job));

        long deliveryTag = 42L;

        when(runPodClient.sendEvent(any(), any(), eq(channel), eq(deliveryTag)))
                .thenAnswer(invocation -> {
                    channel.basicNack(deliveryTag, false, true);
                    return null;
                });

        AnalysisListener listener = new AnalysisListener(
                "http://runpod.invalid/analyse", jobRepository, notificationService, runPodClient, claimService);

        AnalysisCreatedEvent event = new AnalysisCreatedEvent(new AnalysisDTO(correlationId, 1L), null);

        assertDoesNotThrow(() -> listener.listen(event, channel, deliveryTag),
                "the delivery was already nacked by sendEvent; listen() must not also throw "
                        + "and signal the same delivery as failed a second time");
    }
}
