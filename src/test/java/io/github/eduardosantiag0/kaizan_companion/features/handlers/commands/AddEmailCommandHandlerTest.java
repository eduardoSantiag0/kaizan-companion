package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.UserInfoRepository;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.support.TelegramFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


class AddEmailCommandHandlerTest {

    @Test
    void executeCommand_onMissingArgument_mustReplyWithUsage_insteadOfThrowing() {
        TelegramChatRepository chatRepository = mock(TelegramChatRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        UserInfoRepository userInfoRepository = mock(UserInfoRepository.class);

        AddEmailCommandHandler handler =
                new AddEmailCommandHandler(chatRepository, notificationService, userInfoRepository);

        var update = TelegramFixtures.textUpdate(1L, "/add-email");

        assertDoesNotThrow(() -> handler.executeCommand(update));
        verify(notificationService).sendErrorMessage(any(ReplyWithText.class));
    }
}
