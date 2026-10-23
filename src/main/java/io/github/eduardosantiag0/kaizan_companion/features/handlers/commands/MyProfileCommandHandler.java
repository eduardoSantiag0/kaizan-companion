package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.UserInfoEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.UserInfoRepository;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.CommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.utils.MessageFormatter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Service
public class MyProfileCommandHandler implements CommandHandler {

    private final TelegramChatRepository telegramChatRepository;
    private final UserInfoRepository userInfoRepository;
    private final NotificationService notificationService;

    public MyProfileCommandHandler(TelegramChatRepository telegramChatRepository, UserInfoRepository userInfoRepository, NotificationService notificationService) {
        this.telegramChatRepository = telegramChatRepository;
        this.userInfoRepository = userInfoRepository;
        this.notificationService = notificationService;
    }


    @Override
    public ECommands getCommand() {
        return ECommands.MY_PROFILE;
    }

    @Override
    public void executeCommand(Update update) throws TelegramApiException, IOException {

        Long chatId = update.getMessage().getChatId();
        var chat = update.getMessage().getChat();
        TelegramChatEntity chatEntity = telegramChatRepository
                .findById(chatId)
                .orElseGet(() -> new TelegramChatEntity(
                        chatId,
                        chat.getFirstName(),
                        chat.getUserName(),
                        chat.getType(),
                        true
                ));

        telegramChatRepository.save(chatEntity);

        UserInfoEntity userInfo = userInfoRepository
                .findByChat(chatEntity)
                .orElseGet(() -> userInfoRepository.save(
                        new UserInfoEntity(chatEntity)
                ));

        userInfoRepository.save(userInfo);
        notificationService.sendMessageToChat( new ReplyWithText(chatId, MessageFormatter.formatMyProfile(userInfo)));

    }
}
