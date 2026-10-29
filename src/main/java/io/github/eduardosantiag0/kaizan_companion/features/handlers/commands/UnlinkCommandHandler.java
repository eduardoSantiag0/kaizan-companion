package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.UserInfoEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.UserInfoRepository;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.ICommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.utils.MessageFormatter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Service
public class UnlinkCommandHandler implements ICommandHandler {

    private final TelegramChatRepository telegramChatRepository;
    private final UserInfoRepository userInfoRepository;
    private final NotificationService notificationService;

    public UnlinkCommandHandler(TelegramChatRepository telegramChatRepository, UserInfoRepository userInfoRepository, NotificationService notificationService) {
        this.telegramChatRepository = telegramChatRepository;
        this.userInfoRepository = userInfoRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.UNLINK;
    }

    @Override
    public void executeCommand(Update update) throws TelegramApiException, IOException {
        Long chatId = update.getMessage().getChatId();
        String[] fullMessage = update.getMessage().getText().split("\\s+");

        if (fullMessage.length < 2) {
            notificationService.sendErrorMessage(new ReplyWithText(
                    chatId, "Usage: /unlink email|ogs"
            ));
            return;
        }

        String target = fullMessage[1].toLowerCase();
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

        UserInfoEntity userInfo = userInfoRepository
                .findByChat(chatEntity)
                .orElseGet(() -> userInfoRepository.save(new UserInfoEntity(chatEntity)));

        switch (target) {
            case "email" -> userInfo.setEmailAddress(null);
            case "ogs" -> {
                userInfo.setOgsAccountName(null);
                userInfo.setOgsAccountId(null);
            }
            default -> {
                notificationService.sendErrorMessage(new ReplyWithText(
                        chatId, "Unknown target '" + target + "'. Use /unlink email or /unlink ogs"
                ));
                return;
            }
        }

        userInfoRepository.save(userInfo);

        notificationService.sendMessageToChat(new ReplyWithText(
                chatId, MessageFormatter.formatUnlinked(target)
        ));
    }
}
