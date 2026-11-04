package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging.AnalysisProducer;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.features.sources.SourceContextStrategy;
import io.github.eduardosantiag0.kaizan_companion.support.TelegramFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CollectionCommandHandlerTest {

    @Test
    void executeCommand_onNoGamesRequested_mustReplyWithUsage_insteadOfDoingNothing() {
        TelegramChatRepository chatRepository = mock(TelegramChatRepository.class);
        SourceContextStrategy sourceContextStrategy = mock(SourceContextStrategy.class);
        AnalysisProducer analysisProducer = mock(AnalysisProducer.class);
        NotificationService notificationService = mock(NotificationService.class);

        CollectionCommandHandler handler = new CollectionCommandHandler(
                chatRepository, sourceContextStrategy, analysisProducer, notificationService);

        var update = TelegramFixtures.textUpdate(1L, "/collection");

        assertDoesNotThrow(() -> handler.executeCommand(update));
        verify(notificationService).sendErrorMessage(any(ReplyWithText.class));
    }
}
