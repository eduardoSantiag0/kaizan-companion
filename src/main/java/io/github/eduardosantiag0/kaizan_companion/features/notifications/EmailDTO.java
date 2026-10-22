package io.github.eduardosantiag0.kaizan_companion.features.notifications;

public record EmailDTO(
        String to,
        String subject,
        String message,
        String name
) {
}
