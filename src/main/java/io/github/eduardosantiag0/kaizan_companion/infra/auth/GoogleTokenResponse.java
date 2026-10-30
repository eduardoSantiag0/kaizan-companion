package io.github.eduardosantiag0.kaizan_companion.infra.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Long expiresIn,

        @JsonProperty("token_type")
        String tokenType,


        @JsonProperty("scope")
        String scope
) {
}
