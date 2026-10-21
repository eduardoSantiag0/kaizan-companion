package io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.runpod;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.JobStatus;

import java.util.UUID;

public record RunPodCancelledResponse(
        UUID id,
        JobStatus status
) {
}
