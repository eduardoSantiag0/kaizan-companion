package io.github.eduardosantiag0.kaizan_companion.infra.auth.ticket;

import java.time.Instant;

public record TicketData(
        Long chatId,
        Instant expiresAt,
        Boolean used
) {
}
