package io.github.eduardosantiag0.kaizan_companion.features.telegram.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.TelegramBotService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequestMapping("/kaizan/api/v1")
public class TelegramController {
    private static final String TELEGRAM_SECRET_HEADER = "X-Telegram-Bot-Api-Secret-Token";

    private final TelegramBotService service;
    private final String secretToken;
    private final ObjectMapper telegramObjectMapper = new ObjectMapper();


    public TelegramController(TelegramBotService service, @Value("${telegram.bot.token}") String secretToken) {
        this.service = service;
        this.secretToken = secretToken;
    }

    @PostMapping
    public ResponseEntity<Void> onUpdateReceived(
            @RequestBody String rawUpdate,
            HttpServletRequest request) throws JsonProcessingException
    {
        String receivedToken = request.getHeader(TELEGRAM_SECRET_HEADER);

        if (receivedToken == null || !receivedToken.equals(secretToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        Update update = telegramObjectMapper.readValue(rawUpdate, Update.class);
        service.consume(update);
        return ResponseEntity.ok().build();
    }
}
