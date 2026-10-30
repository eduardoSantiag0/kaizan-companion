package io.github.eduardosantiag0.kaizan_companion.features.storage.messaging;

public record DriveStorageEvent(
        Long chatId,
        byte[][] sgfInBytes,
        String players,
        String username
) {
}
