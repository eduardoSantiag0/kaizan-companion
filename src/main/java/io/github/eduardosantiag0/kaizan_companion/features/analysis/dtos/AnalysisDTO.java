package io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos;

import java.util.UUID;

public record AnalysisDTO(
        UUID correlationId,
        Long chatId
) {
}
