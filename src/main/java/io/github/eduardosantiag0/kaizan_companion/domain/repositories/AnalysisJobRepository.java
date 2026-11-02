package io.github.eduardosantiag0.kaizan_companion.domain.repositories;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnalysisJobRepository extends JpaRepository<AnalysisJobEntity, UUID> {
    Optional<AnalysisJobEntity> findByCorrelationId(UUID correlationId);
    List<AnalysisJobEntity> findByChatAndJobStatusIn(
            TelegramChatEntity chat,
            Collection<JobStatus> statuses
    );

    @Modifying
    @Query(value = """
    UPDATE analysis_job_db
    SET job_status = 'SUBMITTING'
    WHERE correlation_id = :correlationId
      AND job_status = 'ANALYSIS_REQUESTED'
    """, nativeQuery = true)
    int claimJob(@Param("correlationId") UUID correlationId);
}
