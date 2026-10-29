package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.utils.GeneralValidator;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.UserInfoEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.UserInfoRepository;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.ICommandHandler;
import io.github.eduardosantiag0.kaizan_companion.utils.MessageFormatter;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Service
public class AddEmailCommandHandler implements ICommandHandler {
    private final TelegramChatRepository telegramChatRepository;
    private final NotificationService notificationService;
    private final UserInfoRepository userInfoRepository;

    public AddEmailCommandHandler(TelegramChatRepository telegramChatRepository, NotificationService notificationService, UserInfoRepository userInfoRepository) {
        this.telegramChatRepository = telegramChatRepository;
        this.notificationService = notificationService;
        this.userInfoRepository = userInfoRepository;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.ADD_EMAIL;
    }

    @Override
    public void executeCommand(Update update) throws TelegramApiException, IOException {
        String[] fullMessage = update.getMessage().getText().split("\\s+");;
        String email = fullMessage[1];
        Long chatId = update.getMessage().getChatId();

        if (!GeneralValidator.isValidEmail(email)) {
            notificationService.sendErrorMessage(new ReplyWithText(
                    chatId, "✕ Invalid Email! ✕"));
            return;
        }

        var chat = update.getMessage().getChat();

        TelegramChatEntity entity = telegramChatRepository
                .findById(chatId)
                .orElseGet(() -> new TelegramChatEntity(
                        chatId,
                        chat.getFirstName(),
                        chat.getUserName(),
                        chat.getType(),
                        true
                ));

        UserInfoEntity userInfo = userInfoRepository
                .findByChat(entity)
                .orElseGet(() -> new UserInfoEntity(entity));

//        if (userInfo.getEmailAddress() != null) {
//            notificationService.sendErrorMessage(new ReplyWithText(
//                    chatId, "✕ Email already linked! Use /unlink email to change it. ✕"));
//            return;
//        }

        userInfo.setEmailAddress(email);

        userInfoRepository.save(userInfo);

        notificationService.sendMessageToChat(new ReplyWithText(
                chatId, MessageFormatter.formatEmailLinked(email)
        ));


    }
}
