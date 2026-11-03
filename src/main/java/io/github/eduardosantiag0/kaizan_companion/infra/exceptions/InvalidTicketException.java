package io.github.eduardosantiag0.kaizan_companion.infra.exceptions;

public class InvalidTicketException extends RuntimeException {
    public InvalidTicketException(String message) {
        super(message);
    }
}
