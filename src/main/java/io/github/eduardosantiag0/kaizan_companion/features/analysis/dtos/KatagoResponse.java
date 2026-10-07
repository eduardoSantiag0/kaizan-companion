package io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos;


import java.util.List;

public record KatagoResponse(
        String id, // The same id string that was provided on the query.
        boolean isDuringSearch,
        List<MoveInformation> moveInfos,
        List<RootInfo> rootInfo,
        int turnNumber
) {
}
