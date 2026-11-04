package io.github.eduardosantiag0.kaizan_companion.exceptions.commands;

public class UnknownCommandException extends RuntimeException {
    public UnknownCommandException(String message) {
        super(message);
    }
}
