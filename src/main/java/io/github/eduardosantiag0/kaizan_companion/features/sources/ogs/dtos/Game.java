package io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Game(
        String related,
        String players,
        Long id,
        Long creator,
        @JsonProperty("sgf_filename")
        String sgfFilename,
        @JsonProperty("historical_rating")
        String historicalRating,
        String flags,
        @JsonProperty("bot_detection_results")
        String botDetectionResults
) {
}
