package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class JobClaimService {

    private final short LEASE_TIME = 300; // 5 minutos

    private final AnalysisJobRepository jobRepository;

    public JobClaimService(AnalysisJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }


    @Transactional
    public boolean claim (AnalysisJobEntity job, UUID correlationId) {
        job.setLeaseUntil(Instant.now().plusSeconds(LEASE_TIME));
        return jobRepository.claimJob(correlationId);
    }

}
