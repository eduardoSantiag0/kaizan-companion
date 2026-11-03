package io.github.eduardosantiag0.kaizan_companion.infra.schedulers;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.OUTBOX_EVENTS;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.OutboxEventEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.OutboxRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class LeaseReleaserScheduler {

    private final AnalysisJobRepository jobRepository;
    private final OutboxRepository outboxRepository;

    public LeaseReleaserScheduler(
            AnalysisJobRepository jobRepository,
            OutboxRepository outboxRepository
    ) {
        this.jobRepository = jobRepository;
        this.outboxRepository = outboxRepository;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void recoverLease() {

        var expired = jobRepository.findExpiredLeases(Instant.now());

        for (var job : expired) {
            job.setJobStatus(JobStatus.ANALYSIS_REQUESTED);
            job.setLeaseUntil(null);
            jobRepository.save(job);

            requeue(job.getCorrelationId());
        }

//        log.info("Jobs recuperados: {}", expired.size());

    }

    private void requeue(UUID correlationId) {
        outboxRepository.findFirstByCorrelationIdOrderByCreatedAtDesc(correlationId)
                .ifPresentOrElse(original -> {
                    var event = new OutboxEventEntity();
                    event.setAggregateType(original.getAggregateType());
                    event.setAggregateId(original.getAggregateId());
                    event.setPayload(original.getPayload());
                    event.setCorrelationId(original.getCorrelationId());
                    event.setType(OUTBOX_EVENTS.ANALYSIS_REQUESTED);

                    outboxRepository.save(event);
                }, () -> log.warn(
                        "Lease expirada para job {}, mas evento de outbox original não encontrado",
                        correlationId
                ));
    }

}
