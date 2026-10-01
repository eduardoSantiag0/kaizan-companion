package io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos;


import java.util.List;

public record PaginatedGameList(
        int count,
        String next,
        String previous,
        List<Game> results
) {
}
