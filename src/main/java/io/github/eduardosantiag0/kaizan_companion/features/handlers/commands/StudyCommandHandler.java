package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging.AnalysisProducer;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.CommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISourceStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.sources.SourceContextStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
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

        if (!isValidURL(url)) {
            return;
        }

        ISourceStrategy source = sourceContextStrategy.setStrategy(url);

        String id = source.extractId(url);

        ByteArrayResource fileData = source.downloadGameById(id);

        if (fileData == null) {
            log.error("file data is null");
            return;
        }


        Long chatId = update.getMessage().getChatId();
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


    }
}
