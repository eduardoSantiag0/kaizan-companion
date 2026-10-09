package io.github.eduardosantiag0.kaizan_companion.features.analysis.katago.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.MoveInformation;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.RootInfo;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record KatagoResponse(
        String id,
        boolean isDuringSearch,
        List<MoveInformation> moveInfos,
        RootInfo rootInfo,
        int turnNumber
) {}
