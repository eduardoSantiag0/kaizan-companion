package io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlayerInfo(
        int id,
        String username,
        String country,
        String icon,
        RatingDTO ratings,
        String ranking,
        boolean professional,
        @JsonProperty("ui_class")
        String uiClass

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
