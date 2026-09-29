package io.github.eduardosantiag0.kaizan_companion.features.telegram;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TelegramChatRepository extends JpaRepository<TelegramChatEntity, UUID> {
}
