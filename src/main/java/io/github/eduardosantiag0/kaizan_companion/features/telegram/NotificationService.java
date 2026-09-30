package io.github.eduardosantiag0.kaizan_companion.features.telegram;

import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.replies.ReplyWithFile;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.replies.ReplyWithText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);

    @Autowired
    private final TelegramClient telegramClient;

    public NotificationService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void sendMessageToChat(ReplyWithFile message) {

        SendMessage response = SendMessage.builder()
                .chatId(message.chatId())
                .text(message.text())
                .build();


        logger.info("\n\nSucesso! \n\n" + response.getText());
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


        logger.info("\n\nSucesso! \n\n" + response.getText());

    }
}
//
//Sucesso!
//SendMessage(chatId=4923, messageThreadId=null, directMessagesTopicId=null, text=Account linked: SuperDU69
//        Now you can track your studies more easile!, parseMode=null, disableWebPagePreview=null, disableNotification=null, replyToMessageId=null, replyMarkup=null, entities=null, allowSendingWithoutReply=null, protectContent=null, linkPreviewOptions=null, replyParameters=null, businessConnectionId=null, messageEffectId=null, allowPaidBroadcast=null, suggestedPostParameters=null, ephemeralMessageParameters=null)
