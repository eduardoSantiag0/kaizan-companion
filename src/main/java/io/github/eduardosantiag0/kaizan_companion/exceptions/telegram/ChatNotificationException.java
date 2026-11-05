package io.github.eduardosantiag0.kaizan_companion.exceptions.telegram;

public class ChatNotificationException extends RuntimeException {

    public ChatNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
