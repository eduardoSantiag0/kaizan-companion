package io.github.eduardosantiag0.kaizan_companion.features.telegram;

import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.replies.ReplyWithFile;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.replies.ReplyWithText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.net.SocketException;
import java.util.concurrent.TimeoutException;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);

    @Autowired
    private final TelegramClient telegramClient;

    public NotificationService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }


    @Retryable(
            retryFor = TelegramApiException .class,
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 1000,
                    multiplier = 2.0,
                    maxDelay = 10000
            )
    )
    public void sendMessageToChat(ReplyWithFile message) {

        SendMessage response = SendMessage.builder()
                .chatId(message.chatId())
                .text(message.text())
                .build();


        logger.info("\nSucesso! \n" + response.getText());
//        try {
//            telegramClient.execute(response);
//        } catch (TelegramApiException e) {
//            logger.error(e.getMessage());
//            throw new RuntimeException(e);
//        }

    }

    public void sendMessageToChat(ReplyWithText message) {

        SendMessage response = SendMessage.builder()
                .chatId(message.chatId())
                .text(message.text())
                .build();


        logger.info("\nSucesso! \n" + response.getText());

    }

    public void sendErrorMessage (ReplyWithText message) {
        SendMessage response = SendMessage.builder()
                .chatId(message.chatId())
                .text(message.text())
                .build();


        logger.info(response.getText());

    }
}

