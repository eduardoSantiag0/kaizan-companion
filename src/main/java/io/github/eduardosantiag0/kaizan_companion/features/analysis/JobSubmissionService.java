package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.OUTBOX_EVENTS;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.OutboxEventEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.OutboxRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.AnalysisCreatedEvent;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Service
public class JobSubmissionService {

    private final AnalysisJobRepository analysisJobRepository;
    private final TelegramChatRepository chatRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper mapper;

    public JobSubmissionService(
            AnalysisJobRepository analysisJobRepository,
            TelegramChatRepository chatRepository, OutboxRepository outboxRepository, ObjectMapper mapper
    ) {
        this.analysisJobRepository = analysisJobRepository;
        this.chatRepository = chatRepository;
        this.outboxRepository = outboxRepository;
        this.mapper = mapper;
    }

    @Transactional
    public void saveJob(
            Long chatId,
            UUID correlationId,
            String source,
            String originalGame,
            AnalysisCreatedEvent event
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


        OutboxEventEntity outboxEvent = new OutboxEventEntity();
        String payload = mapper.writeValueAsString(event);
        outboxEvent.setAggregateType("Analysis");
        outboxEvent.setAggregateId(String.valueOf(event.analysisDTO().chatId()));
        outboxEvent.setType(OUTBOX_EVENTS.ANALYSIS_REQUESTED);
        outboxEvent.setPayload(payload);
        outboxEvent.setCorrelationId(correlationId);

        outboxRepository.save(outboxEvent);

    }
}
