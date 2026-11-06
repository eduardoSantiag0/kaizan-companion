package io.github.eduardosantiag0.kaizan_companion.features.telegram.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.TelegramBotService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import static io.github.eduardosantiag0.kaizan_companion.domain.models.ECommands.*;

@RestController
@RequestMapping("/kaizan/api/v1")
public class TelegramController {
    private static final Logger logger = LoggerFactory.getLogger(TelegramController.class);
    private static final String TELEGRAM_SECRET_HEADER = "X-Telegram-Bot-Api-Secret-Token";

    private static final List<BotCommand> commandList = List.of(
            new BotCommand(START.getCommand().substring(1), "Get started and see what Kaizen Companion can do"),
            new BotCommand(STUDY.getCommand().substring(1), "Upload your SGF to analyse your game"),
            new BotCommand(COLLECTION.getCommand().substring(1), "Upload several games at once"),
            new BotCommand(LAST_GAME.getCommand().substring(1), "Get your latest game"),
            new BotCommand(ADD_ACCOUNT_OGS.getCommand().substring(1), "Add your OGS username to track your games"),
            new BotCommand(ADD_ACCOUNT_DRAGON.getCommand().substring(1), "Add your Dragon Go Server username to track your games"),
            new BotCommand(ADD_EMAIL.getCommand().substring(1), "Link an email to receive your analysed games"),
            new BotCommand(ADD_GOOGLE_DRIVE.getCommand().substring(1), "Connect your Google Drive to store analysed games"),
            new BotCommand(MY_PROFILE.getCommand().substring(1), "Show your linked accounts and email"),
            new BotCommand(UNLINK.getCommand().substring(1), "Unlink your email or OGS account"),
            new BotCommand(QUEUE.getCommand().substring(1), "Show your pending analysis jobs"),
            new BotCommand(RANDOM.getCommand().substring(1), "Analyse a random OGS game"),
            new BotCommand(RESIGN.getCommand().substring(1), "Cancel a running analysis job")
    );

    private final TelegramBotService service;
    private final TelegramClient telegramClient;
    private final String secretToken;
    private final ObjectMapper telegramObjectMapper = new ObjectMapper();

    public TelegramController(
            TelegramBotService service,
            TelegramClient telegramClient,
            @Value("${telegram.webhook.secret-token}") String secretToken) {
        this.service = service;
        this.telegramClient = telegramClient;
        this.secretToken = secretToken;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerBotCommands() {
        try {
            telegramClient.execute(new SetMyCommands(commandList));
        } catch (TelegramApiException e) {
            logger.error("Failed to register bot commands with Telegram", e);
        }
    }

    @PostMapping
    public ResponseEntity<Void> onUpdateReceived(
            @RequestBody String rawUpdate,
            HttpServletRequest request) throws JsonProcessingException
    {
        String receivedToken = request.getHeader(TELEGRAM_SECRET_HEADER);

        if (receivedToken == null || !constantTimeEquals(receivedToken, secretToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        Update update = telegramObjectMapper.readValue(rawUpdate, Update.class);
        service.consume(update);
        return ResponseEntity.ok().build();
    }

    private static boolean constantTimeEquals(String received, String expected) {
        return MessageDigest.isEqual(
                received.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8)
        );
    }
}
