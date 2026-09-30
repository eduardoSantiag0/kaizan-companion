package io.github.eduardosantiag0.kaizan_companion.features.handlers;

import io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface CommandHandler {
    ECommands getCommand();
    void executeCommand(Update update) throws TelegramApiException;

}
