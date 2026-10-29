package io.github.eduardosantiag0.kaizan_companion.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "oauth_connections_db")
@Getter
@Setter
public class OAuthConnectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private TelegramChatEntity chat;

    @Column(nullable = false)
    private String provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "access_token", columnDefinition = "TEXT")
    @Setter
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    @Setter
    private String refreshToken;

    @Column(name = "expires_at_token")
    private Instant expiresAt;


    public OAuthConnectionEntity(TelegramChatEntity chat, String provider, String providerId, Instant expiresAt) {
        this.chat = chat;
        this.provider = provider;
        this.providerId = providerId;
        this.expiresAt = expiresAt;
    }

    public OAuthConnectionEntity() {

    }
}
