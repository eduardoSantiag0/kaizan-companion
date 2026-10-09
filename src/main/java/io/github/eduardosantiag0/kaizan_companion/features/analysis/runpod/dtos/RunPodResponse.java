package io.github.eduardosantiag0.kaizan_companion.features.analysis.runpod.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RunPodResponse(
        String id,
        String status,
        GameAnalysisResponse output
) {}
