package io.github.eduardosantiag0.kaizan_companion.features.telegram.enums;

import io.github.eduardosantiag0.kaizan_companion.features.telegram.CommandHandler;
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

public enum ECommands {
    STUDY ("/study"),
    COLLECTION("/collection"),
    LATEST("/latest"),
    ADD_MY_OGS("add-my-ogs");

    private final String name;

    ECommands(String s) {
        this.name = s;
    }

    public String getName() {
        return this.name;
    }

    @Component
    public static class TelegramBotService extends TelegramLongPollingBot {

        private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);
        private final String botName;
        private final String botToken;
        private final CommandHandler handler;

        private static final List<BotCommand> commandList = List.of(
                new BotCommand(STUDY.name(), "Upload your SGF to analyse your game"),
                new BotCommand(COLLECTION.name(), "Upload several games at once"),
                new BotCommand(LATEST.name(), "Get your latest game fast"),
                new BotCommand(ADD_MY_OGS.name(), "Add your OGS username to track your games")
        );

        public TelegramBotService(
                @Value("${BOT_NAME}") String botName,
                @Value("${TELEGRAM_TOKEN}") String botToken,
                DefaultBotOptions options, CommandHandler handler
        ) {
            super();
    //        super(options, botName);
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
}
