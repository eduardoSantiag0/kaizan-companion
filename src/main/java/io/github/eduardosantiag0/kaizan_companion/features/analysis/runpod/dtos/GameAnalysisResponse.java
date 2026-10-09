package io.github.eduardosantiag0.kaizan_companion.features.analysis.runpod.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GameAnalysisResponse(
        String gameId,
        int totalMoves,
        List<CriticalPoint> criticalPoints
) {}

