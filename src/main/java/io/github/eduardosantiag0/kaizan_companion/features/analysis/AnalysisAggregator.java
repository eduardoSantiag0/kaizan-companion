package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.GameAnalysisEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.telegram.ChatNotificationException;
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
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;


@Service
public class AnalysisAggregator {
    private static final Logger log = LoggerFactory.getLogger(AnalysisAggregator.class);

    private final NotificationService notificationService;
    private final TelegramChatRepository chatRepository;
    private final AnalysisJobRepository jobRepository;
    private final KatagoAssembler assembler; //TODO mudar o nome
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

        System.out.println("CHEGOU: " + response);

        AnalysisJobEntity jobEntity = jobRepository.findByCorrelationId(response.output().correlationId())
                .orElseThrow(() -> new RuntimeException("Job  not found"));

        if (gameAnalysisRepository.existsByJobEntity(jobEntity)) {
            log.warn("Callback duplicado ignorado para o job {}", jobEntity.getId());
            return;
        }

        TelegramChatEntity chatEntity = chatRepository.findById(response.output().chatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        GameAnalysisEntity gameAnalysisEntity = new GameAnalysisEntity(jobEntity, chatEntity);

        List<ByteArrayResource> gamePhases = Collections.synchronizedList(new ArrayList<>());
        String[] sgfs = new String[3];

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            response.output().output().criticalPoints().forEach(point -> {
                executor.submit(() -> {
//                    SgfNode{W[rb]}
                    String node = assembler.attachAnalysisToGame(point, jobEntity.getOriginalGame());
                    byte[] data = SerializationUtils.serialize(node);

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

                    System.out.println(sgfs.toString());

                    // Salvar e notificar com o ByteArrayResource
                    try {
                        storageProvider.store(data);
                        notificationService.sendMessageToChat(
                                new ReplyWithFile(response.output().chatId(),data,
                                        MessageFormatter.requestCompleted(data)
                                )
                        );
//                        gamePhases.add(fileData);
                    } catch (ChatNotificationException e) {
                        throw new ChatNotificationException(e.getMessage());
                    }
//                    catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }


                });
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("Not possible to save your game");
        }

//        gameAnalysisEntity.setFullAnalysedGame(String.valueOf(assembler.assebleAllPhases(gamePhases)));

        gameAnalysisRepository.save(gameAnalysisEntity);

        jobEntity.setJobStatus(JobStatus.COMPLETED);

        chatEntity.setWaitingForAnalysis(false);
    }
}
