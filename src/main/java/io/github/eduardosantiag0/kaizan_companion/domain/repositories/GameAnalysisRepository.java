package io.github.eduardosantiag0.kaizan_companion.domain.repositories;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.GameAnalysisEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.models.GameHistoryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GameAnalysisRepository extends JpaRepository<GameAnalysisEntity, UUID> {
    boolean existsByJobEntity(AnalysisJobEntity jobEntity);

    @Query("""
        SELECT new io.github.eduardosantiag0.kaizan_companion.domain.models.GameHistoryDto(
            g.id, g.jobEntity.correlationId, g.openingAnalysed, g.middlegameAnalysed, g.endgameAnalysed
        )
        FROM GameAnalysisEntity g
        WHERE g.chat = :chat
        ORDER BY g.id
    """)
    List<GameHistoryDto> findHistoryByChat(@Param("chat") TelegramChatEntity chat);
}
