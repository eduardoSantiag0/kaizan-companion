package io.github.eduardosantiag0.kaizan_companion.features.notifications.replies;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;

import java.util.UUID;

public record ReplyWithFile(
        Long chatId,
        byte[] gameBytes,
        String text,
        GamePhase phase,
        UUID id
) {

}
