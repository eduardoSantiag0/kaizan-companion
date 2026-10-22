package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.CommandHandler;
import io.github.eduardosantiag0.kaizan_companion.utils.MessageFormatter;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class QueueCommandHandler implements CommandHandler {

    private final AnalysisJobRepository jobRepository;
    private final TelegramChatRepository telegramChatRepository;
    private final NotificationService notificationService;

    public QueueCommandHandler(AnalysisJobRepository jobRepository, TelegramChatRepository telegramChatRepository, NotificationService notificationService) {
        this.jobRepository = jobRepository;
        this.telegramChatRepository = telegramChatRepository;
        this.notificationService = notificationService;
    }

    private static final Set<JobStatus> ACTIVE_STATUS =
            EnumSet.of(
                    JobStatus.ANALYSIS_REQUESTED,
                    JobStatus.IN_QUEUE
            );

    @Override
    public ECommands getCommand() {
        return ECommands.QUEUE;
    }

    @Override
    public void executeCommand(Update update) throws TelegramApiException, IOException {

        Long chatId = update.getMessage().getChatId();

        var chat = update.getMessage().getChat();
        TelegramChatEntity entity = telegramChatRepository
                .findById(chatId)
                .orElseGet(() -> new TelegramChatEntity(
                        chatId,
                        chat.getFirstName(),
                        chat.getUserName(),
                        chat.getType(),
                        false
                ));

        List<AnalysisJobEntity> jobs =
                jobRepository
                        .findByChatAndJobStatusIn(
                                entity,
                                ACTIVE_STATUS
                        );

        notificationService.sendMessageToChat(new ReplyWithText(
                chatId, MessageFormatter.formatQueue(jobs)
        ));


    }
}
