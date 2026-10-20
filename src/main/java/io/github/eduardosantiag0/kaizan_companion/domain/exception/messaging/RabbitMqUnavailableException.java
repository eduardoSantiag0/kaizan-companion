package io.github.eduardosantiag0.kaizan_companion.domain.exception.messaging;

public class RabbitMqUnavailableException extends RuntimeException {
    public RabbitMqUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
