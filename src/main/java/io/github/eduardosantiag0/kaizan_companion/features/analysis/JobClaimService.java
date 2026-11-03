package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JobClaimService {

    private final short LEASE_TIME = 300; // 5 minutos

    private final AnalysisJobRepository jobRepository;

    public JobClaimService(AnalysisJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public boolean claim(AnalysisJobEntity job) {
        Instant leaseUntil = Instant.now().plusSeconds(LEASE_TIME);
        boolean claimed = jobRepository.claimJob(job.getId(), leaseUntil);

        if (claimed) {
            job.setJobStatus(JobStatus.SUBMITTING);
            job.setLeaseUntil(leaseUntil);
        }

        return claimed;
    }

}
