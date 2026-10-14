package io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaginatedGameList(
        int count,
        String next,
        String previous,
        List<Game> results
) {
}
