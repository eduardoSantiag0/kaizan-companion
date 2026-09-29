package io.github.eduardosantiag0.kaizan_companion.features.telegram.dto;

public record TelegramMessage(
        Long message_id,
        TelegramChat chat,
        String text
) {}
