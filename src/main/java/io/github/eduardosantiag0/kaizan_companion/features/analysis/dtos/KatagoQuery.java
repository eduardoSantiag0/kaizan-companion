package io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.eduardosantiag0.sgf.go.SgfMove;

import java.util.List;
import java.util.Optional;


@JsonIgnoreProperties(ignoreUnknown = true)
public record KatagoQuery(
        String id, // An arbitrary string identifier for the query.
        List<SgfMove> initialStone,
        List<SgfMove> moves,
        String rules,
        float komi,
        int boardXSize,
        int boardYSize,
        List<Integer >analyzeTurns,
        Optional<String> initialPlayer
) {
}
