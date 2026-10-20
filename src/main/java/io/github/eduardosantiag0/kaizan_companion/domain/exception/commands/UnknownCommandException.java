package io.github.eduardosantiag0.kaizan_companion.domain.exception.commands;

public class UnknownCommandException extends RuntimeException {
    public UnknownCommandException(String message) {
        super(message);
    }
}
