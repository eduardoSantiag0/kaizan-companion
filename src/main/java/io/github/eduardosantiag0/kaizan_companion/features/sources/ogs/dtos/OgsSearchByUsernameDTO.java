package io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OgsSearchByUsernameDTO(
        int count,
        String next,
        String previous,
        List<PlayerInfo> results

) {
}
