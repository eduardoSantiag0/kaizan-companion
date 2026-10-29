package io.github.eduardosantiag0.kaizan_companion.infra.auth.ticket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TicketService {

    private final String CONNECT_ENDPOINT;

    private final Map<UUID, TicketData> tickets =
            new ConcurrentHashMap<>();

    public TicketService(
            @Value("${google.connect.endpoint}")
            String CONNECT_ENDPOINT
    ) {
        this.CONNECT_ENDPOINT = CONNECT_ENDPOINT;
    }

    public String generateUrl(Long chatId) {
        var ticket = new TicketData(chatId, Instant.now()
                .plusSeconds(300), false);

        UUID uuid = UUID.randomUUID();
        tickets.put(uuid, ticket);

        return CONNECT_ENDPOINT+"?id="+uuid;

    }

    public Long consume(String id) {

        UUID uuid = UUID.fromString(id);

        TicketData data = tickets.remove(uuid);

        if (data == null) {
            throw new RuntimeException("Ticket not found!");
        }

        if (data.expiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Ticket expired!");
        }

        return data.chatId();
    }

}


