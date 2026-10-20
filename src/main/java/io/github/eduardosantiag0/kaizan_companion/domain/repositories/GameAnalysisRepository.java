package io.github.eduardosantiag0.kaizan_companion.domain.repositories;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.GameAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameAnalysisRepository extends JpaRepository<GameAnalysisEntity, UUID> {
}
