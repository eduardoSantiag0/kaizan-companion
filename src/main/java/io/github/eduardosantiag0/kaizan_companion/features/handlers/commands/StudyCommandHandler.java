package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.CustomExceptionHandler;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.files.FileUploadService;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.CommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.sources.usecases.DownloadGameByIdUseCase;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.MessageFormatter;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.replies.ReplyWithFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static io.github.eduardosantiag0.kaizan_companion._shared.GeneralValidator.isValidURL;

@Service
public class StudyCommandHandler implements CommandHandler {

    private final DownloadGameByIdUseCase downloadGameByIdUseCase;
    private final FileUploadService fileUploadService;
    private final TelegramChatRepository telegramChatRepository;
    private final NotificationService notificationService;

    private final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    public StudyCommandHandler(DownloadGameByIdUseCase downloadGameByIdUseCase, FileUploadService fileUploadService, TelegramChatRepository telegramChatRepository, NotificationService notificationService) {
        this.downloadGameByIdUseCase = downloadGameByIdUseCase;
        this.fileUploadService = fileUploadService;
        this.telegramChatRepository = telegramChatRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.STUDY;
    }

    @Override
    public void executeCommand(Update update) {
        String[] fullMessage = update.getMessage().getText().split("\\s+");;
        String url = fullMessage[1];

        if (!isValidURL(url)) {
            return;
        }

        byte[]fileData =  downloadGameByIdUseCase.execute(url);

        if (fileData == null) {
            logger.error("file data is null");
            return;
        }

        String fileName = fileUploadService.uploadFile(fileData);

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
                        true
                ));

        entity.setWaitingForAnalysis(true);
        telegramChatRepository.save(entity);

        notificationService.sendMessageToChat(
                new ReplyWithFile(chatId, fileData,
                        MessageFormatter.formatGameMessage(fileName)
                )
        );
    }
}
