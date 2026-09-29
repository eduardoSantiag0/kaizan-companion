package io.github.eduardosantiag0.kaizan_companion.dto.telegram;

public record TelegramMessage(
        Long message_id,
        TelegramChat chat,
        String text
) {}
