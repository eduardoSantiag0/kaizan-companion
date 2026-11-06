package io.github.eduardosantiag0.kaizan_companion.features.telegram;


//import io.github.eduardosantiag0.kaizan_companion.features.analysis.AnalysisService;

import io.github.eduardosantiag0.kaizan_companion.exceptions.commands.UnknownCommandException;
import io.github.eduardosantiag0.kaizan_companion.exceptions.telegram.CommandMisuseException;
import io.github.eduardosantiag0.kaizan_companion.features.handlers.commands.*;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.utils.GeneralValidator;
import io.github.eduardosantiag0.kaizan_companion.utils.MessageFormatter;
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
    private final StartCommandHandler startCommandHandler;
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
    private final TelegramClient telegramClient;


    public TelegramBotService(
            @Value("${telegram.bot.name}") String botName,
            @Value("${telegram.bot.token}") String botToken,
            StartCommandHandler startCommandHandler,
            StudyCommandHandler studyCommandHandler,
            TelegramClient telegramClient, AddOgsCommandHandler addOgsCommandHandler, LastGameCommandHandler lastGameCommandHandler, NotificationService notificationService, ResignCommandHandler resignCommandHandler, CollectionCommandHandler collectionCommandHandler, RandomCommandHandler randomCommandHandler, QueueCommandHandler queueCommandHandler, AddEmailCommandHandler addEmailCommandHandler, MyProfileCommandHandler myProfileCommandHandler, UnlinkCommandHandler unlinkCommandHandler, AddDgsCommandHandler addDgsCommandHandler, AddGoogleDriveCommandHandler addGoogleDriveCommandHandler) {
        this.botName = botName;
        this.botToken = botToken;
        this.startCommandHandler = startCommandHandler;
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
        this.telegramClient = telegramClient;
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
        }
        catch (CommandMisuseException e) {
            notifyIfPossible(update, e.getMessage());
        }
        catch (UnknownCommandException e) {
            notifyIfPossible(update, "Your commando does not exist");
        }
        catch (Exception e) {
            logger.error("Failed to handle update", e);
            notifyIfPossible(update, MessageFormatter.requestFailed());
        }
    }

    // update.getMessage() for updates that are not a plain text message (edited messages,
    // channel posts, group membership changes, stickers/photos without a caption is null for those,
    //    so any reply must be skipped rather than NPE.
    private void notifyIfPossible(Update update, String text) {
        if (update.getMessage() == null) {
            logger.warn("Could not notify chat: update {} has no message", update.getUpdateId());
            return;
        }
        notificationService.sendErrorMessage(new ReplyWithText(update.getMessage().getChatId(), text));
    }

    private void handle(Update update) throws TelegramApiException, IOException, CommandMisuseException{
        if (update.getMessage() == null || update.getMessage().getText() == null) {
            logger.debug("Ignoring update {} without a text message", update.getUpdateId());
            return;
        }

        String[] fullMessage = update.getMessage().getText().trim().split("\\s+");
        String command = fullMessage[0];

        if (!command.startsWith("/")) {
            if (GeneralValidator.isValidURL(command)) {
                studyCommandHandler.executeCommand(update);
            } else {
                notificationService.sendErrorMessage(new ReplyWithText(update.getMessage().getChatId(), MessageFormatter.unknownMessage()));
            }
            return;
        }

        switch (command) {
            case "/start" ->
                startCommandHandler.executeCommand(update);

            case "/study" ->
                studyCommandHandler.executeCommand(update);

            case "/add_account_ogs" ->
                addOgsCommandHandler.executeCommand(update);

            case "/add_account_dragon" ->
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

            case "/add_email" ->
                addEmailCommandHandler.executeCommand(update);

            case "/my_profile" ->
                myProfileCommandHandler.executeCommand(update);

            case "/unlink" ->
                unlinkCommandHandler.executeCommand(update);

            case "/add_google_drive" ->
                addGoogleDriveCommandHandler.executeCommand(update);

            default -> {
                notificationService.sendErrorMessage(new ReplyWithText(update.getMessage().getChatId(), "Your commando does not exist"));
                throw new UnknownCommandException("Your commando does not exist");
            }
        }
    }

}
