package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.JobSubmissionService;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging.AnalysisProducer;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.KatagoQueryBuilder;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.OgsGameDownloader;
import io.github.eduardosantiag0.kaizan_companion.support.TelegramFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class RandomCommandHandlerTest {

    @Test
    void executeCommand_mustNotCrash_whenTheDownloadedGameHasNoRankTags() throws Exception {
        String sgfWithoutRanks = "(;GM[1]FF[4]SZ[19]KM[6.5]RU[Chinese]PB[Alice]PW[Bob];B[pd];W[dp])";
        ByteArrayResource gameData =
                new ByteArrayResource(sgfWithoutRanks.getBytes(StandardCharsets.UTF_8));

        OgsGameDownloader ogsGameDownloader = mock(OgsGameDownloader.class);
        when(ogsGameDownloader.downloadGameById(any())).thenReturn(gameData);

        TelegramChatRepository chatRepository = mock(TelegramChatRepository.class);
        when(chatRepository.findById(any())).thenReturn(Optional.empty());

        AnalysisProducer analysisProducer = new AnalysisProducer(
                mock(JobSubmissionService.class),
                new KatagoQueryBuilder(),
                mock(RabbitTemplate.class),
                "analysis-queue"
        );

        RandomCommandHandler handler =
                new RandomCommandHandler(ogsGameDownloader, chatRepository, analysisProducer);

        var update = TelegramFixtures.textUpdate(1L, "/random");

        assertDoesNotThrow(() -> handler.executeCommand(update));
    }
}
