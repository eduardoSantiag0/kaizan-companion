package io.github.eduardosantiag0.kaizan_companion.exceptions.sources;

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
