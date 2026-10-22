package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisDTO;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos.KatagoQuery;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
@Service
public class JobSubmissionService {

    private final AnalysisJobRepository analysisJobRepository;
    private final TelegramChatRepository chatRepository;

    public JobSubmissionService(
            AnalysisJobRepository analysisJobRepository,
            TelegramChatRepository chatRepository
    ) {
        this.analysisJobRepository = analysisJobRepository;
        this.chatRepository = chatRepository;
    }

    @Transactional
    public void saveJob(
            Long chatId,
            UUID correlationId,
            String source,
            String originalGame
    ) {
        TelegramChatEntity chat =
                chatRepository.getReferenceById(chatId);

        AnalysisJobEntity job = new AnalysisJobEntity(
                chat,
                Instant.now(),
                correlationId
        );

        job.setLinkToSource(source);
        job.setOriginalGame(originalGame);
        job.setJobStatus(JobStatus.ANALYSIS_REQUESTED);

        analysisJobRepository.save(job);
    }
}
