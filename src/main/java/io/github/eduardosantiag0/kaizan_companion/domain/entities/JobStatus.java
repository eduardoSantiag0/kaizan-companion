package io.github.eduardosantiag0.kaizan_companion.domain.entities;

public enum JobStatus {
    ANALYSIS_REQUESTED,
    CANCELED,
    FAILED,
    IN_QUEUE,
    COMPLETED,
    TIME_OUT;

    public String getName() {
        return this.name();
    }
}
