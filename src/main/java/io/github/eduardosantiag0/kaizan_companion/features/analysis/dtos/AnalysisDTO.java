package io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos;

import io.github.eduardosantiag0.sgf.model.SgfGameTree;

import java.util.UUID;

public record AnalysisDTO(
//        String sgf,
        UUID correlationId,
        Long chatId
) {
}
