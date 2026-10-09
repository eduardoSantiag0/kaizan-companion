package io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RootInfo(
        double winrate,
        double scoreLead,
        int visits,
        String currentPlayer
) {}
