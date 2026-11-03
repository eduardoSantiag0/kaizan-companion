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


class StudyCommandHandlerTest {

    @Test
    void executeCommand_onMissingUrlArgument_mustReplyWithUsage_insteadOfThrowing() {
        SourceContextStrategy sourceContextStrategy = mock(SourceContextStrategy.class);
        TelegramChatRepository chatRepository = mock(TelegramChatRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        AnalysisProducer analysisProducer = mock(AnalysisProducer.class);

        StudyCommandHandler handler = new StudyCommandHandler(
                sourceContextStrategy, chatRepository, notificationService, analysisProducer);

        var update = TelegramFixtures.textUpdate(1L, "/study");

        assertDoesNotThrow(() -> handler.executeCommand(update));
        verify(notificationService).sendErrorMessage(any(ReplyWithText.class));
    }
}
