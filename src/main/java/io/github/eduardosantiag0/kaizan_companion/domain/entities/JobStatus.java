package io.github.eduardosantiag0.kaizan_companion.domain.entities;

public enum JobStatus {
    ANALYSIS_REQUESTED,
    CANCELLED,
    FAILED,
    IN_QUEUE,
    COMPLETED,
    TIMED_OUT;
    public String getName() {
        return this.name();
    }
}

