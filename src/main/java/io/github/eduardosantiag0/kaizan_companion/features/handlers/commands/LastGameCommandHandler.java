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
import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.infra.IStorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Service
public class LastGameCommandHandler implements CommandHandler {

    private final TelegramChatRepository telegramChatRepository;
    private final NotificationService notificationService;
    private final SourceContextStrategy sourceContextStrategy;
    private final IStorageProvider storageProvider;

    private final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    public LastGameCommandHandler(TelegramChatRepository telegramChatRepository, NotificationService notificationService, SourceContextStrategy sourceContextStrategy, IStorageProvider storageProvider) {
        this.telegramChatRepository = telegramChatRepository;
        this.notificationService = notificationService;
        this.sourceContextStrategy = sourceContextStrategy;
        this.storageProvider = storageProvider;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.LAST_GAME;
    }

    @Override
    public void executeCommand(Update update) throws TelegramApiException, IOException {
        Long chatId = update.getMessage().getChatId();

        TelegramChatEntity entity = telegramChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if ((entity.getOgsAccountName() == null) || (entity.getOgsAccountId() == null)) {
            notificationService.sendErrorMessage(new ReplyWithText(chatId, "You must link your account first!\n"));
            return;
        }


        //TODO como resolver isso?
        ISourceStrategy source = sourceContextStrategy.setStrategy("https://online-go.com");

        ByteArrayResource fileData = source.downloadLastGame(entity.getOgsAccountId());

        if (fileData == null) {
            logger.error("file data is null");
            return;
        } else logger.debug(fileData.toString());

        storageProvider.store(fileData.getContentAsByteArray());


        entity.setWaitingForAnalysis(true);
        telegramChatRepository.save(entity);


        notificationService.sendMessageToChat(
                new ReplyWithFile(chatId, fileData.getContentAsByteArray(),
                        MessageFormatter.formatGameMessage(String.valueOf(fileData))
                )
        );

    }
}
