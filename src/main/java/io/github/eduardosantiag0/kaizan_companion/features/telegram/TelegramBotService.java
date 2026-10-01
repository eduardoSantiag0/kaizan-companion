package io.github.eduardosantiag0.kaizan_companion.features.telegram;


//import io.github.eduardosantiag0.kaizan_companion.features.analysis.AnalysisService;

import io.github.eduardosantiag0.kaizan_companion.features.handlers.commands.AddOgsCommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.commands.LastGameCommandHandler;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.commands.StudyCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;



import java.util.List;

import static io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands.*;

@Service
public class TelegramBotService
        implements
        SpringLongPollingBot,
        LongPollingSingleThreadUpdateConsumer
{

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);
    private final String botName;
    private final String botToken;
    private final StudyCommandHandler studyCommandHandler;
    private final AddOgsCommandHandler addOgsCommandHandler;
    private final LastGameCommandHandler lastGameCommandHandler;


    private static final List<BotCommand> commandList = List.of(
            new BotCommand(STUDY.name(), "Upload your SGF to analyse your game"),
            new BotCommand(COLLECTION.name(), "Upload several games at once"),
            new BotCommand(LAST_GAME.name(), "Get your latest game fast"),
            new BotCommand(ADD_MY_OGS.name(), "Add your OGS username to track your games")
    );

    public TelegramBotService(
            @Value("${telegram.bot.name}") String botName,
            @Value("${telegram.bot.token}") String botToken,
            StudyCommandHandler studyCommandHandler,
            TelegramClient telegramClient, AddOgsCommandHandler addOgsCommandHandler, LastGameCommandHandler lastGameCommandHandler) {
        this.botName = botName;
        this.botToken = botToken;
        this.studyCommandHandler = studyCommandHandler;
        this.addOgsCommandHandler = addOgsCommandHandler;
        this.lastGameCommandHandler = lastGameCommandHandler;
    }

    public String getBotUsername() {
        return this.botName;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        try {
            handle(update);
        } catch (Exception e) {
            logger.error("Failed to handle update", e);
        }
    }

    private void handle(Update update) throws TelegramApiException {
        String[] fullMessage = update.getMessage().getText().split("\\s+");;
        String command = fullMessage[0];


        switch (command) {
            case "/study" ->
                studyCommandHandler.executeCommand(update);

            case "/add-ogs-account" ->
                addOgsCommandHandler.executeCommand(update);

            case "/last_game" ->
                lastGameCommandHandler.executeCommand(update);


        }
    }

}
