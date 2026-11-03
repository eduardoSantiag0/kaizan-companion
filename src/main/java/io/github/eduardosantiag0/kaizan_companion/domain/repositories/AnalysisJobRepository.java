package io.github.eduardosantiag0.kaizan_companion.domain.repositories;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.JobStatus;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
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

    @Query(value = """
    SELECT * FROM analysis_job_db
    WHERE job_status = 'SUBMITTING'
      AND lease_until < :now
    FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<AnalysisJobEntity> findExpiredLeases(@Param("now") Instant now);

    @Modifying
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
    UPDATE analysis_job_db
    SET job_status = 'SUBMITTING'
    WHERE correlation_id = :correlationId
      AND job_status = 'ANALYSIS_REQUESTED'
    FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    boolean claimJob(@Param("correlationId") UUID correlationId);
}
