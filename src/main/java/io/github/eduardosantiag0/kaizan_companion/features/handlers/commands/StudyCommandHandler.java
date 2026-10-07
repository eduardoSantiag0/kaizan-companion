package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.CustomExceptionHandler;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.CommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISourceStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.sources.SourceContextStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.MessageFormatter;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.replies.ReplyWithFile;
import io.github.eduardosantiag0.kaizan_companion.infra.IStorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;

import static io.github.eduardosantiag0.kaizan_companion._shared.GeneralValidator.isValidURL;

@Service
public class StudyCommandHandler implements CommandHandler {

    private final SourceContextStrategy sourceContextStrategy;
    private final TelegramChatRepository telegramChatRepository;
    private final NotificationService notificationService;
    private final IStorageProvider storageProvider;

    private final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    public StudyCommandHandler(SourceContextStrategy sourceContextStrategy, TelegramChatRepository chatRepository, NotificationService notificationService, IStorageProvider storageProvider) {
        this.sourceContextStrategy = sourceContextStrategy;
        this.telegramChatRepository = chatRepository;
        this.notificationService = notificationService;
        this.storageProvider = storageProvider;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.STUDY;
    }



    @Override
    public void executeCommand(Update update) throws IOException {
        String[] fullMessage = update.getMessage().getText().split("\\s+");

        String url = fullMessage[1];

        if (!isValidURL(url)) {
            return;
        }

        ISourceStrategy source = sourceContextStrategy.setStrategy(url);

        String id = source.extractId(url);

        ByteArrayResource fileData = source.downloadGameById(id);

        if (fileData == null) {
            logger.error("file data is null");
            return;
        }

        storageProvider.store(fileData.getContentAsByteArray());

        Long chatId = update.getMessage().getChatId();
        var chat = update.getMessage().getChat();
        TelegramChatEntity entity = telegramChatRepository
                .findById(chatId)
                .orElseGet(() -> new TelegramChatEntity(
                        chatId,
                        chat.getFirstName(),
                        chat.getUserName(),
                        chat.getType(),
                        null,
                        null,
                        null,
                        true
                ));

        entity.setWaitingForAnalysis(true);
        telegramChatRepository.save(entity);

        notificationService.sendMessageToChat(
                new ReplyWithFile(chatId, fileData.getContentAsByteArray(),
                        MessageFormatter.formatGameMessage(fileData.getContentAsByteArray())
                )
        );
    }
}
