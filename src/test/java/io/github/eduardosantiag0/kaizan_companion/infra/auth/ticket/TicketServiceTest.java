package io.github.eduardosantiag0.kaizan_companion.infra.auth.ticket;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;


class TicketServiceTest {

    private final TicketService ticketService = new TicketService("https://example.com/connect/google");

    @SuppressWarnings("unchecked")
    private void injectExpiredTicket(UUID id, Long chatId) throws Exception {
        Field field = TicketService.class.getDeclaredField("tickets");
        field.setAccessible(true);
        Map<UUID, TicketData> tickets = (Map<UUID, TicketData>) field.get(ticketService);
        tickets.put(id, new TicketData(chatId, Instant.now().minusSeconds(1), false));
    }

    @Test
    void consume_returnsTheChatIdForAFreshTicket() {
        String url = ticketService.generateUrl(42L);
        String id = url.substring(url.indexOf("id=") + 3);

        assertEquals(42L, ticketService.consume(id));
    }

    @Test
    void consume_onANonUuidId_mustNotLeakRawIllegalArgumentException() {
        Exception ex = assertThrows(Exception.class, () -> ticketService.consume("not-a-uuid"));
        assertFalse(ex instanceof IllegalArgumentException,
                "UUID.fromString()'s parsing exception leaked out unhandled");
    }

    @Test
    void consume_onAnUnknownButWellFormedUuid_throwsTicketNotFound() {
        String randomId = UUID.randomUUID().toString();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> ticketService.consume(randomId));
        assertEquals("Ticket not found!", ex.getMessage());
    }

    @Test
    void consume_onAnExpiredTicket_throwsTicketExpired() throws Exception {
        UUID id = UUID.randomUUID();
        injectExpiredTicket(id, 7L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> ticketService.consume(id.toString()));
        assertEquals("Ticket expired!", ex.getMessage());
    }

    @Test
    void consume_isSingleUse() {
        String url = ticketService.generateUrl(99L);
        String id = url.substring(url.indexOf("id=") + 3);

        ticketService.consume(id);

        assertThrows(RuntimeException.class, () -> ticketService.consume(id));
    }
}
