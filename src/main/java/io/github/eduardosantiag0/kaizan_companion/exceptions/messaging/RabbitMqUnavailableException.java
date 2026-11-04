package io.github.eduardosantiag0.kaizan_companion.exceptions.messaging;

public class RabbitMqUnavailableException extends RuntimeException {
    public RabbitMqUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
