package io.github.eduardosantiag0.kaizan_companion.features.telegram;

import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.TelegramSendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);

    @Autowired
    private final TelegramClient telegramClient;

    public NotificationService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void sendMessageToChat(TelegramSendMessage message) {

        SendMessage response = SendMessage.builder().chatId(message.chatId())
                .text(message.text())
                .build();


        logger.info("Sucesso! " + response);
//        try {
//            telegramClient.execute(response);
//        } catch (TelegramApiException e) {
//            logger.error(e.getMessage());
//            throw new RuntimeException(e);
//        }


    }
}
