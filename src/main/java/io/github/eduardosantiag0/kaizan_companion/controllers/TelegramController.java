package io.github.eduardosantiag0.kaizan_companion.controllers;

import io.github.eduardosantiag0.kaizan_companion.dto.telegram.TelegramUpdate;
import io.github.eduardosantiag0.kaizan_companion.services.TelegramBotService;
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

    public TelegramController(TelegramBotService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> onUpdateReceived(
            @RequestBody Update update) {

        service.onUpdateReceived(update);
        return ResponseEntity.ok().build();
    }
}
