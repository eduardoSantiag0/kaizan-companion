package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import com.sun.net.httpserver.HttpServer;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.UserInfoRepository;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.support.TelegramFixtures;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class AddDgsCommandHandlerTest {

    @Test
    void executeCommand_onMissingArgument_mustReplyWithUsage_insteadOfThrowing() {
        // Arrange
        TelegramChatRepository chatRepository = mock(TelegramChatRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        UserInfoRepository userInfoRepository = mock(UserInfoRepository.class);

        AddDgsCommandHandler handler = new AddDgsCommandHandler(
                "https://dragongoserver.net",
                chatRepository,
                notificationService,
                userInfoRepository
        );

        var update = TelegramFixtures.textUpdate(
                1L,
                "/add-account-dragon"
        );

        // Act
        assertDoesNotThrow(() -> handler.executeCommand(update));

        // Assert
        verify(notificationService, times(1))
                .sendErrorMessage(any(ReplyWithText.class));

        verifyNoInteractions(chatRepository, userInfoRepository);
    }


    @Test
    void executeCommand_mustNotFetchAUrlOnAHostOtherThanDragonGoServer() throws Exception {
        AtomicBoolean serverWasContacted = new AtomicBoolean(false);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            serverWasContacted.set(true);
            byte[] body = "{\"uid\":\"42\",\"handle\":\"attacker\",\"rating\":1500.0,\"rank\":\"1d\"}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            int port = server.getAddress().getPort();
            String maliciousUrl = "http://127.0.0.1:" + port + "/internal-admin-panel?uid=42";

            TelegramChatRepository chatRepository = mock(TelegramChatRepository.class);
            NotificationService notificationService = mock(NotificationService.class);
            UserInfoRepository userInfoRepository = mock(UserInfoRepository.class);
            when(chatRepository.findById(any())).thenReturn(Optional.empty());

            AddDgsCommandHandler handler =
                    new AddDgsCommandHandler("https://dragongoserver.net/", chatRepository, notificationService, userInfoRepository);

            var update = TelegramFixtures.textUpdate(1L, "/add-account-dragon " + maliciousUrl);

            try {
                handler.executeCommand(update);
            } catch (Exception ignored) {
            }

            assertFalse(serverWasContacted.get(),
                    "the handler fetched a URL on a host other than dragongoserver.net");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void executeCommand_mustNotBeFooledByTheTrustedDomainAppearingInsideTheUrl() throws Exception {
        // Regression test: the old check was `profileUrl.contains(DGS_BASE_URL)`, a plain
        // substring search. Embedding the trusted URL as a query parameter value on an
        // attacker-controlled host made that check pass while the real GET request still
        // went to the attacker's host (SSRF) - e.g. the Oracle Cloud metadata endpoint.
        AtomicBoolean serverWasContacted = new AtomicBoolean(false);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            serverWasContacted.set(true);
            byte[] body = "{\"uid\":\"42\",\"handle\":\"attacker\",\"rating\":1500.0,\"rank\":\"1d\"}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            int port = server.getAddress().getPort();
            String maliciousUrl = "http://127.0.0.1:" + port
                    + "/pwn?uid=42&x=https://dragongoserver.net/";

            TelegramChatRepository chatRepository = mock(TelegramChatRepository.class);
            NotificationService notificationService = mock(NotificationService.class);
            UserInfoRepository userInfoRepository = mock(UserInfoRepository.class);

            AddDgsCommandHandler handler =
                    new AddDgsCommandHandler("https://dragongoserver.net/", chatRepository, notificationService, userInfoRepository);

            var update = TelegramFixtures.textUpdate(1L, "/add-account-dragon " + maliciousUrl);

            assertDoesNotThrow(() -> handler.executeCommand(update));

            assertFalse(serverWasContacted.get(),
                    "the handler fetched a URL whose host is not dragongoserver.net, "
                            + "even though the trusted domain appeared inside the URL");
        } finally {
            server.stop(0);
        }
    }
}
