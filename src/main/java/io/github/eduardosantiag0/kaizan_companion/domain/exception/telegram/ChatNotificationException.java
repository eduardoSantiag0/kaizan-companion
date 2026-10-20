package io.github.eduardosantiag0.kaizan_companion.domain.exception.telegram;

public class ChatNotificationException extends RuntimeException {
    public ChatNotificationException(String message) {
        super(message);
    }
}
