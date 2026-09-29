package io.github.eduardosantiag0.kaizan_companion.services;

import io.github.eduardosantiag0.kaizan_companion.services.enums.ECommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class TelegramBotService extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);
    private final String botName;
    private final String botToken;
    private final CommandHandler handler;

    private static final List<BotCommand> commandList = List.of(
            new BotCommand(ECommands.STUDY.name(), "Upload your SGF to analyse your game"),
            new BotCommand(ECommands.COLLECTION.name(), "Upload several games at once"),
            new BotCommand(ECommands.LATEST.name(), "Get your latest game fast"),
            new BotCommand(ECommands.ADD_MY_OGS.name(), "Add your OGS username to track your games")
    );

    public TelegramBotService(
            @Value("${BOT_NAME}") String botName,
            @Value("${TELEGRAM_TOKEN}") String botToken,
            DefaultBotOptions options, CommandHandler handler
    ) {
        super(options);
        this.botName = botName;
        this.botToken = botToken;
        this.handler = handler;
        try {
            this.execute(new SetMyCommands(commandList));
        } catch (TelegramApiException e) {
            logger.error("Failed to set bot commands: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return this.botName;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        handler.execute(update);

    }
}
