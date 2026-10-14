package io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos;

public enum JobStatus {
    ANALYSIS_REQUESTED,
    ANALYSIS_CANCELED,
    SENT_TO_ANALYSIS,
    REQUEST_FAILED,
    ENGINE_ANALYSIS_DONE,
    ANALYSIS_COMPLETED;

    public String getName() {
        return this.name();
    }
}
