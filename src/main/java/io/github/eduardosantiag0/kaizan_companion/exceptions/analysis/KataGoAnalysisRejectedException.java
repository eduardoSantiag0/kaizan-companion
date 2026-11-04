package io.github.eduardosantiag0.kaizan_companion.exceptions.analysis;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.errors.KatagoErrorMessage;

public class KataGoAnalysisRejectedException extends RuntimeException {
    public KataGoAnalysisRejectedException(KatagoErrorMessage message) {
        super(String.valueOf(message));
    }
}
