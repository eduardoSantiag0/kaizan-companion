package io.github.eduardosantiag0.kaizan_companion.features.telegram;


//import io.github.eduardosantiag0.kaizan_companion.features.analysis.AnalysisService;

import io.github.eduardosantiag0.kaizan_companion.domain.exception.commands.UnknownCommandException;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.commands.*;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
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


import java.io.IOException;
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
    private final NotificationService notificationService;
    private final ResignCommandHandler resignCommandHandler;
    private final CollectionCommandHandler collectionCommandHandler;
    private final RandomCommandHandler randomCommandHandler;
    private final QueueCommandHandler queueCommandHandler;
    private final AddEmailCommandHandler addEmailCommandHandler;
    private final MyProfileCommandHandler myProfileCommandHandler;
    private final UnlinkCommandHandler unlinkCommandHandler;
    private final AddDgsCommandHandler addDgsCommandHandler;
    private final AddGoogleDriveCommandHandler addGoogleDriveCommandHandler;


    private static final List<BotCommand> commandList = List.of(
            new BotCommand(STUDY.name(), "Upload your SGF to analyse your game"),
            new BotCommand(COLLECTION.name(), "Upload several games at once"),
            new BotCommand(LAST_GAME.name(), "Get your latest game"),
            new BotCommand(ADD_ACCOUNT_OGS.name(), "Add your OGS username to track your games"),
            new BotCommand(ADD_ACCOUNT_DRAGON.name(), "Add your Dragon Go Server username to track your games")
    );

    public TelegramBotService(
            @Value("${telegram.bot.name}") String botName,
            @Value("${telegram.bot.token}") String botToken,
            StudyCommandHandler studyCommandHandler,
            TelegramClient telegramClient, AddOgsCommandHandler addOgsCommandHandler, LastGameCommandHandler lastGameCommandHandler, NotificationService notificationService, ResignCommandHandler resignCommandHandler, CollectionCommandHandler collectionCommandHandler, RandomCommandHandler randomCommandHandler, QueueCommandHandler queueCommandHandler, AddEmailCommandHandler addEmailCommandHandler, MyProfileCommandHandler myProfileCommandHandler, UnlinkCommandHandler unlinkCommandHandler, AddDgsCommandHandler addDgsCommandHandler, AddGoogleDriveCommandHandler addGoogleDriveCommandHandler) {
        this.botName = botName;
        this.botToken = botToken;
        this.studyCommandHandler = studyCommandHandler;
        this.addOgsCommandHandler = addOgsCommandHandler;
        this.lastGameCommandHandler = lastGameCommandHandler;
        this.notificationService = notificationService;
        this.resignCommandHandler = resignCommandHandler;
        this.collectionCommandHandler = collectionCommandHandler;
        this.randomCommandHandler = randomCommandHandler;
        this.queueCommandHandler = queueCommandHandler;
        this.addEmailCommandHandler = addEmailCommandHandler;
        this.myProfileCommandHandler = myProfileCommandHandler;
        this.unlinkCommandHandler = unlinkCommandHandler;
        this.addDgsCommandHandler = addDgsCommandHandler;
        this.addGoogleDriveCommandHandler = addGoogleDriveCommandHandler;
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

    private void handle(Update update) throws TelegramApiException, IOException {
        String[] fullMessage = update.getMessage().getText().split("\\s+");;
        String command = fullMessage[0];


        switch (command) {
            case "/study" ->
                studyCommandHandler.executeCommand(update);

            case "/add-account-ogs" ->
                addOgsCommandHandler.executeCommand(update);

            case "/add-account-dragon" ->
                addDgsCommandHandler.executeCommand(update);

            case "/last_game" ->
                lastGameCommandHandler.executeCommand(update);

            case "/resign" ->
                resignCommandHandler.executeCommand(update);

            case "/collection" ->
                collectionCommandHandler.executeCommand(update);

            case "/random" ->
                randomCommandHandler.executeCommand(update);

            case "/queue" ->
                queueCommandHandler.executeCommand(update);

            case "/add-email" ->
                addEmailCommandHandler.executeCommand(update);

            case "/my-profile" ->
                myProfileCommandHandler.executeCommand(update);

            case "/unlink" ->
                unlinkCommandHandler.executeCommand(update);
            case "/add-google-drive" ->
                addGoogleDriveCommandHandler.executeCommand(update);

            default -> {
                notificationService.sendErrorMessage(new ReplyWithText(update.getMessage().getChatId(), "Your commando does not exist"));
                throw new UnknownCommandException("Your commando does not exist");
            }
        }
    }

}
