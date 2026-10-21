package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod.RunPodCancelledResponse;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod.RunPodResponse;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.CommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.replies.ReplyWithText;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Service
public class ResignCommandHandler implements CommandHandler {

    record CancelRequest(String correlationId){}

    private final RestTemplate rest = new RestTemplate();
    private final NotificationService notificationService;

    @Value("${katago.runpod.status.url}")
    private String RUNPOD_URL;

    private final String endpoint = "/cancel";

    public ResignCommandHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public ECommands getCommand() {
        return ECommands.RESIGN;
    }

    @Override
    public void executeCommand(Update update) throws TelegramApiException, IOException {
        String[] fullMessage = update.getMessage().getText().split("\\s+");

        String jobId = fullMessage[1];

        RunPodCancelledResponse response = rest.postForObject(
                RUNPOD_URL+endpoint,
                new CancelRequest(jobId),
                RunPodCancelledResponse.class
        );

        String text;

        if (response.status() == JobStatus.CANCELLED) {
            text = "Job: " + jobId + " was cancelled\n";
        } else {
            text = "It was not possible to cancel job: " + jobId;

        }

        notificationService.sendMessageToChat(
                new ReplyWithText(update.getMessage().getChatId(),
                        text
                )
        );
    }
}
