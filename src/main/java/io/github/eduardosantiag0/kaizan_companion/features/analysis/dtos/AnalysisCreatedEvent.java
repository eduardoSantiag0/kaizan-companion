package io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos;

public record AnalysisCreatedEvent(
        AnalysisDTO analysisDTO,
        KatagoQuery katagoQuery
) {
}
