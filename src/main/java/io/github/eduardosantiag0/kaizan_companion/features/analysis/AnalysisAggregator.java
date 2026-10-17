package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.KatagoAssembler;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod.RunPodResponse;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.MessageFormatter;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.replies.ReplyWithFile;
import io.github.eduardosantiag0.kaizan_companion.infra.IStorageProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;


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

        System.out.println("CHEGOU: " + response);

        AnalysisJobEntity jobEntity = jobRepository.findByCorrelationId(response.output().correlationId())
                .orElseThrow(() -> new RuntimeException("Job  not found"));

        TelegramChatEntity chatEntity = chatRepository.findById(response.output().chatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        List<ByteArrayResource> gamePhases = Collections.synchronizedList(new ArrayList<>());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            response.output().output().criticalPoints().forEach(point -> {
                executor.submit(() -> {

                    ByteArrayResource fileData = assembler.attachAnalysisToGame(point, jobEntity.getOriginalGame());

                    // Salvar o sgf na entidade
                    switch (point.phase()) {
                        case GamePhase.opening -> jobEntity.setOpeningAnalysed(fileData.toString());
                        case GamePhase.middlegame -> jobEntity.setMiddlegameAnalysed(fileData.toString());
                        case GamePhase.endgame -> jobEntity.setEndgameAnalysed(fileData.toString());
                    }

                    // Salvar e notificar com o ByteArrayResource
                    try {
                        storageProvider.store(fileData.getContentAsByteArray());
                        notificationService.sendMessageToChat(
                                new ReplyWithFile(response.output().chatId(), fileData.getContentAsByteArray(),
                                        MessageFormatter.requestCompleted(fileData.getContentAsByteArray())
                                )
                        );
                        gamePhases.add(fileData);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }


                });
            });

        }

        jobEntity.setFullAnalysedGame(String.valueOf(assembler.assebleAllPhases(gamePhases)));

        jobEntity.setJobStatus(JobStatus.COMPLETED);

        chatEntity.setWaitingForAnalysis(false);
    }
}
