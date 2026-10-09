package io.github.eduardosantiag0.kaizan_companion.features.analysis.runpod.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos.KatagoResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CriticalPoint(
        String phase,
        int turn,
        String player,
        String move,
        double winrateDrop,
        KatagoResponse before,
        KatagoResponse after
) {}
