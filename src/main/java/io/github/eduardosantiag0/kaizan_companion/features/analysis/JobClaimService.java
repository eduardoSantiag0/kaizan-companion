package io.github.eduardosantiag0.kaizan_companion.features.analysis;

import io.github.eduardosantiag0.kaizan_companion.domain.repositories.AnalysisJobRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobClaimService {

    private final AnalysisJobRepository jobRepository;

    public JobClaimService(AnalysisJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }


    public boolean claim (UUID correlationId) {
//        return jobRepository.claimJob(correlationId);
        return true;
    }

}
