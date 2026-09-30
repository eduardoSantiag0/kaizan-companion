package io.github.eduardosantiag0.kaizan_companion.domain.repositories;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelegramChatRepository extends JpaRepository<TelegramChatEntity, Long> {
    Optional<TelegramChatEntity> findById(Long id);
}
