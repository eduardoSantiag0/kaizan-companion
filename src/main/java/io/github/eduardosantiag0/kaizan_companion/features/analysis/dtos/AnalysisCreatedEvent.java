package io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos.KatagoQuery;

public record AnalysisCreatedEvent(
        AnalysisDTO analysisDTO,
        KatagoQuery katagoQuery
) {
}
