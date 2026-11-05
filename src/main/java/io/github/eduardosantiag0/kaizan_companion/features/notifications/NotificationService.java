package io.github.eduardosantiag0.kaizan_companion.features.notifications;

import io.github.eduardosantiag0.kaizan_companion.exceptions.telegram.ChatNotificationException;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithFile;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;

@Service
@Slf4j
public class NotificationService {

    private static final String ANALYSIS_FILE_NAME = "analysis.sgf";

    private final TelegramClient telegramClient;

    public NotificationService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Retryable(
            retryFor = TelegramApiException.class,
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 1000,
                    multiplier = 2.0,
                    maxDelay = 10000
            )
    )
    public void sendMessageToChat(ReplyWithFile message) {

        SendDocument request = SendDocument.builder()
                .chatId(message.chatId())
                .document(new InputFile(new ByteArrayInputStream(message.gameBytes()), ANALYSIS_FILE_NAME))
                .caption(message.text())
                .build();

        try {
            telegramClient.execute(request);
            log.info("Arquivo enviado com sucesso para o chat {}", message.chatId());
        } catch (TelegramApiException e) {
            log.error("Failed to send file to chat {}", message.chatId(), e);
            throw new ChatNotificationException(
                    "Could not deliver analysis file to chat " + message.chatId(), e
            );
        }
    }

    @Retryable(
            retryFor = TelegramApiException.class,
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 1000,
                    multiplier = 2.0,
                    maxDelay = 10000
            )
    )
    public void sendMessageToChat(ReplyWithText message) {
        dispatch(message.chatId(), message.text());
    }

    public void sendErrorMessage(ReplyWithText message) {
        dispatch(message.chatId(), message.text());
    }

    private void dispatch(Long chatId, String text) {
        SendMessage request = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();

        try {
            telegramClient.execute(request);
            log.info("Mensagem enviada com sucesso para o chat {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}", chatId, e);
            throw new ChatNotificationException(
                    "Could not deliver message to chat " + chatId, e
            );
        }
    }
}
