package io.github.eduardosantiag0.kaizan_companion.features.notifications.messaging;

public record EmailNotificationEvent(
        String to,
        byte[][] sgfInBytes,
        String players,
        String username
) {
}
