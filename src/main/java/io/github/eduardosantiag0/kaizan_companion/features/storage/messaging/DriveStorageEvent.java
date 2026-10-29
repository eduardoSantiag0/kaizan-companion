package io.github.eduardosantiag0.kaizan_companion.features.storage.messaging;

public record DriveStorageEvent(
        byte[][] sgfInBytes,
        String players,
        String username
) {
}
