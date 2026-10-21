package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.GameAnalysisEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.GameAnalysisRepository;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Service
public class AnalysisAggregator {
    private static final Logger log = LoggerFactory.getLogger(AnalysisAggregator.class);

    private final NotificationService notificationService;
    private final TelegramChatRepository chatRepository;
    private final AnalysisJobRepository jobRepository;
    private final KatagoAssembler assembler;
    private final IStorageProvider storageProvider;
    private final GameAnalysisRepository gameAnalysisRepository;


    public AnalysisAggregator(NotificationService notificationService, TelegramChatRepository chatRepository, AnalysisJobRepository jobRepository, KatagoAssembler assembler, IStorageProvider storageProvider, GameAnalysisRepository gameAnalysisRepository) {
        this.notificationService = notificationService;
        this.chatRepository = chatRepository;
        this.jobRepository = jobRepository;
        this.assembler = assembler;
        this.storageProvider = storageProvider;
        this.gameAnalysisRepository = gameAnalysisRepository;
    }

    public void handleResponse(RunPodResponse response) throws IOException {

        AnalysisJobEntity jobEntity = jobRepository.findByCorrelationId(response.output().correlationId())
                .orElseThrow(() -> new RuntimeException("Job  not found"));

        if (gameAnalysisRepository.existsByJobEntity(jobEntity)) {
            log.warn("Callback duplicado ignorado para o job {}", jobEntity.getId());
            return;
        }

        TelegramChatEntity chatEntity = chatRepository.findById(response.output().chatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        GameAnalysisEntity gameAnalysisEntity = new GameAnalysisEntity(jobEntity, chatEntity);

        String[] sgfs = new String[3];
        List<Future<?>> futures = new ArrayList<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            response.output().output().criticalPoints()
                    .forEach(point -> futures.add(
                            executor.submit(() -> {
                String node = assembler.attachAnalysisToGame(point, jobEntity.getOriginalGame());
                byte[] data = node.getBytes(StandardCharsets.UTF_8);

                switch (point.phase()) {
                    case GamePhase.opening -> {
                        sgfs[0] = node;
                        gameAnalysisEntity.setOpeningAnalysed(node);
                    }
                    case GamePhase.middlegame -> {
                        sgfs[1] = node;
                        gameAnalysisEntity.setMiddlegameAnalysed(node);
                    }
                    case GamePhase.endgame -> {
                        sgfs[2] = node;
                        gameAnalysisEntity.setEndgameAnalysed(node);
                    }
                }

                // Salvar e notificar com o ByteArrayResource
                storageProvider.store(data, point.phase());
                notificationService.sendMessageToChat(
                        new ReplyWithFile(response.output().chatId(), data,
                                MessageFormatter.requestCompleted(data)
                        )
                );
            })));
        }

        for (Future<?> future : futures) {
            try {
                future.get(); // Relança o erro caso houve algum
            } catch (ExecutionException e) {
                throw new RuntimeException("Not possible to save your game", e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Not possible to save your game", e);
            }
        }

//        gameAnalysisEntity.setFullAnalysedGame(String.valueOf(assembler.assebleAllPhases(gamePhases)));

        gameAnalysisRepository.save(gameAnalysisEntity);

        jobEntity.setJobStatus(JobStatus.COMPLETED);

        chatEntity.setWaitingForAnalysis(false);
    }
}
