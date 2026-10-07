package io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GameDetail(
        String id,
        String players,
        String related,
        int creator,
        @JsonProperty("historical_ratings")
        String  historicalRatings,
        String gamedata,
        String auth,
        String flags,
        @JsonProperty("bot_detection_results")
        String botDetectionResults,
        @JsonProperty("simul_black")
        String simulBlack,
        @JsonProperty("simul_white")
        String simulWhite
) {
}
