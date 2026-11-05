package io.github.eduardosantiag0.kaizan_companion.features.notifications;

import io.github.eduardosantiag0.kaizan_companion.exceptions.telegram.ChatNotificationException;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithFile;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Test
    void sendMessageToChat_withText_mustActuallyCallTheTelegramClient() throws TelegramApiException {
        TelegramClient telegramClient = mock(TelegramClient.class);
        NotificationService service = new NotificationService(telegramClient);

        service.sendMessageToChat(new ReplyWithText(42L, "hello"));

        verify(telegramClient).execute(any(SendMessage.class));
    }

    @Test
    void sendErrorMessage_mustActuallyCallTheTelegramClient() throws TelegramApiException {
        TelegramClient telegramClient = mock(TelegramClient.class);
        NotificationService service = new NotificationService(telegramClient);

        service.sendErrorMessage(new ReplyWithText(42L, "something went wrong"));

        verify(telegramClient).execute(any(SendMessage.class));
    }

    @Test
    void sendMessageToChat_withFile_mustSendTheGameBytesAsADocument_insteadOfDiscardingThem() throws TelegramApiException {
        TelegramClient telegramClient = mock(TelegramClient.class);
        NotificationService service = new NotificationService(telegramClient);

        byte[] sgfBytes = "(;GM[1]FF[4])".getBytes(StandardCharsets.UTF_8);

        service.sendMessageToChat(new ReplyWithFile(42L, sgfBytes, "your analysis is ready"));

        verify(telegramClient).execute(any(SendDocument.class));
    }

    @Test
    void sendMessageToChat_whenTelegramApiFails_mustWrapTheFailure_insteadOfSwallowingIt() throws TelegramApiException {
        TelegramClient telegramClient = mock(TelegramClient.class);
        when(telegramClient.execute(any(SendMessage.class))).thenThrow(new TelegramApiException("boom"));

        NotificationService service = new NotificationService(telegramClient);

        assertThrows(ChatNotificationException.class,
                () -> service.sendMessageToChat(new ReplyWithText(42L, "hello")));
    }
}
