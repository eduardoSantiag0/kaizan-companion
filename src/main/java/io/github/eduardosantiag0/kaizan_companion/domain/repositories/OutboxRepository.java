package io.github.eduardosantiag0.kaizan_companion.domain.repositories;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.OUTBOX_EVENTS;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.OutboxEventEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEventEntity, UUID> {
    @Transactional
    long deleteByType(OUTBOX_EVENTS type);

    @Modifying
    @Transactional
    @Query("UPDATE OutboxEventEntity o SET o.type = :type, o.published = true WHERE o.id = :id")
    int markAsDone(@Param("id") UUID id, @Param("type") OUTBOX_EVENTS type);
}
