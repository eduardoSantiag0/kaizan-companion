package io.github.eduardosantiag0.kaizan_companion.domain.exception.sources;

public class InvalidGameUrlException extends RuntimeException {
    public InvalidGameUrlException(String message) {
        super(message);
    }

    public static class LatestGameNotFoundException extends RuntimeException {
        public LatestGameNotFoundException(String message) {
            super(message);
        }
    }
}
