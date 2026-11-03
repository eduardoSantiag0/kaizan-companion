package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.UserInfoEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.GameAnalysisRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.UserInfoRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod.GameAnalysisResponse;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod.RunPodResponse;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.KatagoAssembler;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.messaging.EmailNotificationProducer;
import io.github.eduardosantiag0.kaizan_companion.features.storage.messaging.DriveStorageProducer;
import io.github.eduardosantiag0.kaizan_companion.infra.IStorageProvider;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalysisAggregatorTest {

    @Test
    void handleResponse_mustNotCrash_whenKataGoReportsNoCriticalPoints_andTheChatHasEmailLinked() throws Exception {
        UUID correlationId = UUID.randomUUID();
        Long chatId = 1L;

        TelegramChatEntity chatEntity = new TelegramChatEntity(chatId, "Tester", "tester", "private", true);
        AnalysisJobEntity jobEntity = new AnalysisJobEntity(chatEntity, Instant.now(), correlationId);

        UserInfoEntity userInfo = new UserInfoEntity(chatEntity);
        userInfo.setEmailAddress("tester@example.com");

        AnalysisJobRepository jobRepository = mock(AnalysisJobRepository.class);
        when(jobRepository.findByCorrelationId(correlationId)).thenReturn(Optional.of(jobEntity));

        GameAnalysisRepository gameAnalysisRepository = mock(GameAnalysisRepository.class);
        when(gameAnalysisRepository.existsByJobEntity(jobEntity)).thenReturn(false);

        TelegramChatRepository chatRepository = mock(TelegramChatRepository.class);
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chatEntity));

        UserInfoRepository userInfoRepository = mock(UserInfoRepository.class);
        when(userInfoRepository.findByChat(chatEntity)).thenReturn(Optional.of(userInfo));

        AnalysisAggregator aggregator = new AnalysisAggregator(
                mock(NotificationService.class),
                chatRepository,
                jobRepository,
                new KatagoAssembler(),
                mock(IStorageProvider.class),
                gameAnalysisRepository,
                userInfoRepository,
                mock(EmailNotificationProducer.class),
                mock(DriveStorageProducer.class)
        );

        RunPodResponse response = new RunPodResponse(
                "runpod-id",
                new RunPodResponse.JobInformations(
                        chatId, correlationId, "COMPLETED",
                        new GameAnalysisResponse("game-id", 0, List.of())
                )
        );

        assertDoesNotThrow(() -> aggregator.handleResponse(response));
    }
}
