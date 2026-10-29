package io.github.eduardosantiag0.kaizan_companion.domain.repositories;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.OAuthConnectionEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OAuthConnectionRepository extends JpaRepository<OAuthConnectionEntity, UUID> {
    Optional<OAuthConnectionEntity> findByChat(TelegramChatEntity chat);
}
