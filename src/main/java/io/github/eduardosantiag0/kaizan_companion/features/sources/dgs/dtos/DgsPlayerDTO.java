package io.github.eduardosantiag0.kaizan_companion.features.sources.dgs.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DgsPlayerDTO(
        @JsonProperty("uid") String userId,
        @JsonProperty("handle") String username,
        @JsonProperty("rating") Double rating,
        @JsonProperty("rank") String rank
) {}
