package io.github.eduardosantiag0.kaizan_companion.features.telegram.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.eduardosantiag0.kaizan_companion.features.telegram.TelegramBotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequestMapping("/kaizan/api/v1")
public class TelegramController {
    private final TelegramBotService service;
    private final ObjectMapper telegramObjectMapper = new ObjectMapper();

    public TelegramController(TelegramBotService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> onUpdateReceived(
            @RequestBody String rawUpdate) throws JsonProcessingException {

        Update update = telegramObjectMapper.readValue(rawUpdate, Update.class);
        service.consume(update);
        return ResponseEntity.ok().build();
    }
}
