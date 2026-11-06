package io.github.eduardosantiag0.kaizan_companion.features.telegram.controllers;

import io.github.eduardosantiag0.kaizan_companion.features.telegram.TelegramBotService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TelegramControllerTest {

    @Test
    void onUpdateReceived_mustNotDispatchAnUnauthenticatedForgedUpdate() throws Exception {
        TelegramBotService service = mock(TelegramBotService.class);
        TelegramClient telegramClient = mock(TelegramClient.class);
        TelegramController controller = new TelegramController(service, telegramClient, "correct-secret-token");

        HttpServletRequest forgedRequest = mock(HttpServletRequest.class);

        String forgedUpdateJson = """
                {
                  "update_id": 1,
                  "message": {
                    "message_id": 1,
                    "chat": { "id": 999999999, "type": "private" },
                    "text": "/add-email attacker@evil.example"
                  }
                }
                """;

        try {
            controller.onUpdateReceived(forgedUpdateJson, forgedRequest);
        } catch (Exception ignored) {
        }

        verify(service, never()).consume(any(Update.class));
    }
}
