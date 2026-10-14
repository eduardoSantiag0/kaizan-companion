package io.github.eduardosantiag0.kaizan_companion.features.analysis.runpod.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RunPodResponse(
        Long chatId,
        UUID correlationId,
        String id,
        String status,
        GameAnalysisResponse output
) {}
