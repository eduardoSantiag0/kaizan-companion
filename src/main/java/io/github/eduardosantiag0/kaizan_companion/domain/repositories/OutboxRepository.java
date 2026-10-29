package io.github.eduardosantiag0.kaizan_companion.domain.repositories;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.OUTBOX_EVENTS;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.OutboxEventEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEventEntity, UUID> {
    @Transactional
    long deleteByType(OUTBOX_EVENTS type);
}
