package io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos.KatagoResponse;


@JsonIgnoreProperties(ignoreUnknown = true)
public record CriticalPoint(
        GamePhase phase,
        int turn,
        String player,
        String move, // wrong move
        double winrateDrop,
        KatagoResponse before,
        KatagoResponse after
) {}
