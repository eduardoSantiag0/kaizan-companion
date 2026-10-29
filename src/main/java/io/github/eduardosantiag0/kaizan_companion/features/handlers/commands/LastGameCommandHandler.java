package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.UserInfoEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.GlobalExceptionHandler;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.sgf.EmptySgfException;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.sources.OgsAccountNotConfiguredException;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.UserInfoRepository;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.CommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.OgsGameDownloader;
import io.github.eduardosantiag0.kaizan_companion.utils.MessageFormatter;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithFile;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
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
    private final OgsGameDownloader ogsGameDownloader;
    private final UserInfoRepository userInfoRepository;
    private final IStorageProvider storageProvider;

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public LastGameCommandHandler(TelegramChatRepository telegramChatRepository, NotificationService notificationService, OgsGameDownloader ogsGameDownloader, UserInfoRepository userInfoRepository, IStorageProvider storageProvider) {
        this.telegramChatRepository = telegramChatRepository;
        this.notificationService = notificationService;
        this.ogsGameDownloader = ogsGameDownloader;
        this.userInfoRepository = userInfoRepository;
        this.storageProvider = storageProvider;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.LAST_GAME;
    }

    @Override
    public void executeCommand(Update update) throws TelegramApiException, IOException {
        Long chatId = update.getMessage().getChatId();

        TelegramChatEntity chat = telegramChatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        UserInfoEntity user = userInfoRepository.findByChat(chat)
                .orElse(new UserInfoEntity(chat));

        if ((user.getOgsAccountName() == null) || (user.getOgsAccountId() == null)) {
            notificationService.sendErrorMessage(new ReplyWithText(chatId, "You must link your OGS account first!\n"));
            throw new OgsAccountNotConfiguredException("Chat:" + chatId);
        }


        ByteArrayResource fileData = ogsGameDownloader.downloadLastGame(user.getOgsAccountId());

        if (fileData == null) {
            logger.error("file data is null");
            throw new EmptySgfException("Chat:" + chatId);
        } else logger.debug(fileData.toString());


        chat.setWaitingForAnalysis(true);
        telegramChatRepository.save(chat);


        notificationService.sendMessageToChat(
                new ReplyWithFile(chatId, fileData.getContentAsByteArray(),
                        MessageFormatter.requestCompleted(String.valueOf(fileData))
                )
        );

    }
}
