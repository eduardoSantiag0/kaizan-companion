package io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MinimalPlayer (
        Long id,
        String username,
        String icon,
        RatingDTO ratings,
        String ranking,
        String country,
        @JsonProperty("ui_class")
        String uiClass,
        boolean professional

) {
    record RatingDTO(
            int version,
            Overall overall

    ) {
        record Overall(
                Double rating,
                Double deviation,
                Double volatility
        ){}
    }


}
