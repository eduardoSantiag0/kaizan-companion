package io.github.eduardosantiag0.kaizan_companion.domain.exception.sources;

public class LastGameNotFoundException extends RuntimeException {
    public LastGameNotFoundException(String message) {
        super(message);
    }
}
