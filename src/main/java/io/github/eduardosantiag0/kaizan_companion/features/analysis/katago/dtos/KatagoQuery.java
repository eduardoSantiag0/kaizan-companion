package io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.eduardosantiag0.sgf.go.SgfMove;
import lombok.Builder;

import java.util.List;
import java.util.Optional;


@JsonIgnoreProperties(ignoreUnknown = true)
@Builder()
public record KatagoQuery(
        String id, // An arbitrary string identifier for the query.
        List<SgfMove> initialStone,
        List<SgfMove> moves,
        String rules,
        float komi,
        int boardXSize,
        int boardYSize,
        List<Integer >analyzeTurns,
        Optional<String> initialPlayer,
        boolean includePolicy,
        int maxVisits,
        OverridingSettings overrridingSettings
) {
    public record OverridingSettings(
            String humanSLProfile
    ) {
    }
}

//{
//        "id": "game-123-move-48",
//
//        "boardXSize": 19,
//        "boardYSize": 19,
//
//        "moves": [
//        ["B", "Q16"],
//        ["W", "D4"]
//        ],
//
//        "rules": "japanese",
//        "komi": 6.5,
//
//        "includePolicy": true,
//
//        "overrideSettings": {
//        "humanSLProfile": "rank_12k"
//        }
//        }
