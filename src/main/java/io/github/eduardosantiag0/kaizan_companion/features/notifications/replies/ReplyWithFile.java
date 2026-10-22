package io.github.eduardosantiag0.kaizan_companion.features.notifications.replies;

public record ReplyWithFile(
        Long chatId,
        byte[] gameBytes,
        String text
) {

}
