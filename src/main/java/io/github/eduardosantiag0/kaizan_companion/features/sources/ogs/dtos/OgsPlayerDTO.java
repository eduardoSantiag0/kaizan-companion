package io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Date;

public record OgsPlayerDTO(
        String related,
        String username,
        String ranking,
        String name,
        String ratings,
        String is_friend,
        String agaId,
        String uiClass,
        String icon,



        /* Not required fields */

        String language,
        String about,
        boolean supporter,
        @JsonProperty("is_bot")
        boolean isBot,
        @JsonProperty("bot_ai")
        String botAi,
        @JsonProperty("bot_owner")
        boolean botOwner,
        String website,
        @JsonProperty("registration_date")
        Date registrationDate,
        @JsonProperty("timeout_provisional")
        boolean timeoutProvisional
) {
}
