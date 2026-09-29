package io.github.eduardosantiag0.kaizan_companion.dto.telegram;

import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramUpdate extends Update {
    private final TelegramMessage message;

    public TelegramUpdate(TelegramMessage message) {
        this.message = message;
    }
}
//        (TelegramMessage message) {}
