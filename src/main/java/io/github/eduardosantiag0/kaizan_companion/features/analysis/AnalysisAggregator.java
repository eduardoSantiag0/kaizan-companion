package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.KatagoAssembler;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.runpod.dtos.RunPodResponse;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.MessageFormatter;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.replies.ReplyWithFile;
import io.github.eduardosantiag0.kaizan_companion.infra.IStorageProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AnalysisAggregator {
    private final NotificationService notificationService;
    private final TelegramChatRepository chatRepository;
    private final AnalysisJobRepository jobRepository;
    private final KatagoAssembler assembler; //TODO mudar o nome
    private final IStorageProvider storageProvider;


    public AnalysisAggregator(NotificationService notificationService, TelegramChatRepository chatRepository, AnalysisJobRepository jobRepository, KatagoAssembler assembler, IStorageProvider storageProvider) {
        this.notificationService = notificationService;
        this.chatRepository = chatRepository;
        this.jobRepository = jobRepository;
        this.assembler = assembler;
        this.storageProvider = storageProvider;
    }

    public void handleResponse(RunPodResponse response) throws IOException {

        System.out.println("CHEGOU: " +  response);

        AnalysisJobEntity jobEntity = jobRepository.findByCorrelationId(response.output().correlationId()).orElseThrow(() -> new RuntimeException("Job nao encontrado"));

        ByteArrayResource fileData = assembler.execute(response.output().output(),
                jobEntity.getSgfGame());


        TelegramChatEntity chatEntity = chatRepository.findById(response.output().chatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        jobEntity.setJobStatus(JobStatus.COMPLETED);

        jobEntity.setFinished(true);
        jobEntity.setSgfGame(fileData.toString());
        chatEntity.setWaitingForAnalysis(false);
        storageProvider.store(fileData.getContentAsByteArray());

        notificationService.sendMessageToChat(
                new ReplyWithFile(response.output().chatId(), fileData.getContentAsByteArray(),
                        MessageFormatter.requestCompleted(fileData.getContentAsByteArray())
                )
        );
    }
}
