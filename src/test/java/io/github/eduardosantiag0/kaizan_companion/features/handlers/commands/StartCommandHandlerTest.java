package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.support.TelegramFixtures;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StartCommandHandlerTest {

    @Test
    void executeCommand_onNewChat_mustRegisterChatAndReplyWithGuide() {
        TelegramChatRepository chatRepository = mock(TelegramChatRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        when(chatRepository.findById(1L)).thenReturn(Optional.empty());

        StartCommandHandler handler = new StartCommandHandler(chatRepository, notificationService);

        var update = TelegramFixtures.textUpdate(1L, "/start");

        assertDoesNotThrow(() -> handler.executeCommand(update));

        verify(chatRepository).save(any(TelegramChatEntity.class));
        verify(notificationService).sendMessageToChat(any(ReplyWithText.class));
    }

    @Test
    void executeCommand_onExistingChat_mustStillReplyWithGuide() {
        TelegramChatRepository chatRepository = mock(TelegramChatRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        TelegramChatEntity existing = new TelegramChatEntity(1L, "Tester", "tester", "private", false);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(existing));

        StartCommandHandler handler = new StartCommandHandler(chatRepository, notificationService);

        var update = TelegramFixtures.textUpdate(1L, "/start");

        assertDoesNotThrow(() -> handler.executeCommand(update));

        verify(chatRepository).save(existing);
        verify(notificationService).sendMessageToChat(any(ReplyWithText.class));
    }
}
