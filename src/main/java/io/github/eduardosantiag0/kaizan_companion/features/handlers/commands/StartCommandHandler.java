package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.ICommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.utils.MessageFormatter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Service
public class StartCommandHandler implements ICommandHandler {

    private final TelegramChatRepository telegramChatRepository;
    private final NotificationService notificationService;

    public StartCommandHandler(TelegramChatRepository telegramChatRepository, NotificationService notificationService) {
        this.telegramChatRepository = telegramChatRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.START;
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
                        false
                ));

        telegramChatRepository.save(chatEntity);

        notificationService.sendMessageToChat(
                new ReplyWithText(chatId, MessageFormatter.formatStart(chatEntity.getFirstName()))
        );
    }
}
