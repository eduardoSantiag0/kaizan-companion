package io.github.eduardosantiag0.kaizan_companion.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "analysis_job_db")
@Getter
public class AnalysisJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Getter
    private UUID id;

    @Column(name = "correlation_id")
    private UUID correlationId;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private TelegramChatEntity chat;

    @Column(name = "link_source")
    @Setter
    private String linkToSource;

    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "started_at")
    @Setter
    private Instant startedAt;

    @LastModifiedDate
    @Column(name = "last_modified_at")
    private Instant lastModifiedAt;

    @Column(name = "finished_at")
    @Setter
    private Instant finishedAt;

    public AnalysisJobEntity(TelegramChatEntity chat, Instant startedAt,
                             UUID correlationId) {
        this.chat = chat;
        this.startedAt = startedAt;
        this.correlationId = correlationId;
    }

    public AnalysisJobEntity() {

    }
}
