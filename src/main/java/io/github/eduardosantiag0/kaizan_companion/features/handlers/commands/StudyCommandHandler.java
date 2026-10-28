package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging.AnalysisProducer;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.CommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISourceStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.sources.SourceContextStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.utils.MessageFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;

import static io.github.eduardosantiag0.kaizan_companion.utils.GeneralValidator.isValidURL;

@Service
@Slf4j
public class StudyCommandHandler implements CommandHandler {

    private final SourceContextStrategy sourceContextStrategy;
    private final TelegramChatRepository telegramChatRepository;
    private final NotificationService notificationService;
    private final AnalysisProducer analysisProducer;


    public StudyCommandHandler(SourceContextStrategy sourceContextStrategy, TelegramChatRepository chatRepository,
                               NotificationService notificationService, AnalysisProducer analysisProducer) {
        this.sourceContextStrategy = sourceContextStrategy;
        this.telegramChatRepository = chatRepository;
        this.notificationService = notificationService;
        this.analysisProducer = analysisProducer;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.STUDY;
    }



    @Override
    public void executeCommand(Update update) throws IOException {
        String[] fullMessage = update.getMessage().getText().split("\\s+");

        String url = fullMessage[1];
        Long chatId = update.getMessage().getChatId();

        try {
            if (!isValidURL(url)) {
                notificationService.sendErrorMessage(new ReplyWithText(chatId, MessageFormatter.requestFailed()));
                return;
            }

            ISourceStrategy source = sourceContextStrategy.setStrategy(url);

            if (source == null) {
                notificationService.sendErrorMessage(new ReplyWithText(chatId, MessageFormatter.requestFailed()));
                return;
            }

            String id = source.extractId(url);

            ByteArrayResource fileData = source.downloadGameById(id);

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

            entity.setWaitingForAnalysis(true);
            telegramChatRepository.save(entity);

            analysisProducer.execute(chatId, fileData);

        } catch (RuntimeException e) {
            log.error("Failed to process study request for url {}", url, e);
            notificationService.sendErrorMessage(new ReplyWithText(chatId, MessageFormatter.requestFailed()));
        }
    }
}
