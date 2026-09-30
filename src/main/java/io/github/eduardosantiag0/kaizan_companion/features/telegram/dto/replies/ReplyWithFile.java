package io.github.eduardosantiag0.kaizan_companion.features.telegram.dto.replies;

public record ReplyWithFile(
        Long chatId,
        byte[] gameBytes,
        String text
) {

}
