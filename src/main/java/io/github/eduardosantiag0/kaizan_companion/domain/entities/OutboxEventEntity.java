package io.github.eduardosantiag0.kaizan_companion.domain.entities;

import jakarta.persistence.*;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event_db")
@Setter
public class OutboxEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // qual tipo de entidade de negócio está envolvida.
    @Column(name = "aggregate_type")
    private String aggregateType;

    // 	A entidade relacionada
    @Column(name = "aggregate_id")
    private String aggregateId;

    // Nome do evento
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private OUTBOX_EVENTS type;

    // Dados em json
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    private boolean published = false;
}
