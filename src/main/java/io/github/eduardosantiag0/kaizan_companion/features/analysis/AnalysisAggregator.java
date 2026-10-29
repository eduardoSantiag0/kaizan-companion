package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.*;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.GameAnalysisRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.UserInfoRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.KatagoAssembler;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod.RunPodResponse;
import io.github.eduardosantiag0.kaizan_companion.infra.providers.email.EmailService;
import io.github.eduardosantiag0.kaizan_companion.utils.FilenamePattern;
import io.github.eduardosantiag0.kaizan_companion.utils.MessageFormatter;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithFile;
import io.github.eduardosantiag0.kaizan_companion.infra.IStorageProvider;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final EmailService emailService;
    private final UserInfoRepository userInfoRepository;


    public AnalysisAggregator(NotificationService notificationService, TelegramChatRepository chatRepository, AnalysisJobRepository jobRepository, KatagoAssembler assembler, IStorageProvider storageProvider, GameAnalysisRepository gameAnalysisRepository, EmailService emailService, UserInfoRepository userInfoRepository) {
        this.notificationService = notificationService;
        this.chatRepository = chatRepository;
        this.jobRepository = jobRepository;
        this.assembler = assembler;
        this.storageProvider = storageProvider;
        this.gameAnalysisRepository = gameAnalysisRepository;
        this.emailService = emailService;
        this.userInfoRepository = userInfoRepository;
    }

    public void handleResponse(RunPodResponse response) throws IOException, MessagingException {

        AnalysisJobEntity jobEntity = jobRepository.findByCorrelationId(response.output().correlationId())
                .orElseThrow(() -> new RuntimeException("Job  not found"));

        if (gameAnalysisRepository.existsByJobEntity(jobEntity)) {
            log.warn("Callback duplicado ignorado para o job {}", jobEntity.getId());
            return;
        }

        TelegramChatEntity chatEntity = chatRepository.findById(response.output().chatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        GameAnalysisEntity gameAnalysisEntity = new GameAnalysisEntity(jobEntity, chatEntity);

        List<Future<?>> futures = new ArrayList<>();
        String[] sgfs = new String[3];
        byte[][] sgfsInBytes = new byte[3][];

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            response.output().output().criticalPoints()
                    .forEach(point -> futures.add(
                            executor.submit(() -> {
                String node = assembler.attachAnalysisToGame(point, jobEntity.getOriginalGame());
                byte[] data = node.getBytes(StandardCharsets.UTF_8);

                switch (point.phase()) {
                    case GamePhase.opening -> {
                        sgfsInBytes[0] = data;
                        sgfs[0] = node;
                        gameAnalysisEntity.setOpeningAnalysed(node);
                    }
                    case GamePhase.middlegame -> {
                        sgfsInBytes[1] = data;
                        sgfs[1] = node;
                        gameAnalysisEntity.setMiddlegameAnalysed(node);
                    }
                    case GamePhase.endgame -> {
                        sgfsInBytes[2] = data;
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

        Optional<UserInfoEntity> optionalUser = userInfoRepository.findByChat(chatEntity);

        if (optionalUser.isPresent() && optionalUser.get().getEmailAddress() != null) {
            UserInfoEntity userEnity = optionalUser.get();
            emailService.sendEmail(userEnity.getEmailAddress(), sgfsInBytes,
                    FilenamePattern.getPlayersName(sgfsInBytes[0]), chatEntity.getUsername());

        }

    }
}
