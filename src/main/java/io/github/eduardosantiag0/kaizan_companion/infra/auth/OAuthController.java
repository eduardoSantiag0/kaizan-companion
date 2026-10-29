package io.github.eduardosantiag0.kaizan_companion.infra.auth;

import io.github.eduardosantiag0.kaizan_companion.infra.auth.ticket.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
@RequestMapping("/connect")
@Slf4j
public class OAuthController {

    private final TicketService ticketService;
    private final String GOOGLE_AUTH_ENDPOINT;

    public OAuthController(
            TicketService ticketService,
            @Value("${google.auth.endpoint}")
            String GOOGLE_AUTH_ENDPOINT
    ) {
        this.GOOGLE_AUTH_ENDPOINT = GOOGLE_AUTH_ENDPOINT;
        this.ticketService = ticketService;
    }

    @GetMapping("/google")
    public void connect(
            @RequestParam String id,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        Long chatId = ticketService.consume(id);

        log.debug("Encontrado o id: {}", chatId.toString());

        // Salva na sessão
        request.getSession(true)
                .setAttribute("telegramChatId", chatId);

        log.debug(request.getSession().getAttributeNames().toString());

        response.sendRedirect(GOOGLE_AUTH_ENDPOINT);

    }

}
