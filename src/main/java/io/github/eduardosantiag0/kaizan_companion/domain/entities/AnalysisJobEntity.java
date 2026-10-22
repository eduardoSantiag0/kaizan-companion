package io.github.eduardosantiag0.kaizan_companion.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analysis_job_db")
@Getter
@Slf4j
@EntityListeners(AuditingEntityListener.class)
public class AnalysisJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Getter
    private UUID id;

    @Column(name = "correlation_id")
    private UUID correlationId;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    @Setter
    private TelegramChatEntity chat;


    @Column(name = "original_game",
            columnDefinition = "TEXT")
    @Setter
    @Getter
    private String originalGame;

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

    @Column(name = "job_status")
    @Enumerated(EnumType.STRING)
    private JobStatus jobStatus;

    public void setJobStatus(JobStatus newStatus) {
//        if (newStatus == this.jobStatus) throw new RuntimeException("Status already set");

        var oldStatus = this.jobStatus;
        this.jobStatus = newStatus;

        log.debug("{} --> {}", oldStatus,this.jobStatus);

    }



    public AnalysisJobEntity(TelegramChatEntity chat, Instant startedAt,
                             UUID correlationId) {
        this.chat = chat;
        this.startedAt = startedAt;
        this.correlationId = correlationId;
    }

    public AnalysisJobEntity() {

    }
}
