package io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod;

import java.util.UUID;

//@JsonIgnoreProperties(ignoreUnknown = true)


public record RunPodResponse(
        String id, // Id dentro do KataGo
        JobInformations output

) {
    public record JobInformations(
            Long chatId,
            UUID correlationId, // correlation id do Job
            String status,
            GameAnalysisResponse output
            ) {}
}
