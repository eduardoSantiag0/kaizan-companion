package io.github.eduardosantiag0.kaizan_companion.domain.exception.analysis;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.errors.KatagoErrorMessage;

public class KataGoAnalysisRejectedException extends RuntimeException {
    public KataGoAnalysisRejectedException(KatagoErrorMessage message) {
        super(message);
    }
}
