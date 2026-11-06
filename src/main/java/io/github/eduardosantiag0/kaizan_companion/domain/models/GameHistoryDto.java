package io.github.eduardosantiag0.kaizan_companion.domain.models;

import java.util.UUID;

public record GameHistoryDto(
        UUID id,
        UUID correlationId,
        String openingAnalysed,
        String middlegameAnalysed,
        String endgameAnalysed
) {
}
