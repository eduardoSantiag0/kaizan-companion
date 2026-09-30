package io.github.eduardosantiag0.kaizan_companion.features.telegram.dto;

public record TelegramSendMessage(
        Long chatId,
        byte[] gameBytes,
        String text
) {

}
