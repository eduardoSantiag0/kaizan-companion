package io.github.eduardosantiag0.kaizan_companion.features.handlers.commands;

import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.UserInfoRepository;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.NotificationService;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.replies.ReplyWithText;
import io.github.eduardosantiag0.kaizan_companion.support.TelegramFixtures;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


class AddOgsCommandHandlerTest {

    @Test
    void executeCommand_onMissingArgument_mustReplyWithUsage_insteadOfThrowing() {
        TelegramChatRepository chatRepository = mock(TelegramChatRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        UserInfoRepository userInfoRepository = mock(UserInfoRepository.class);

        AddOgsCommandHandler handler =
                new AddOgsCommandHandler(chatRepository, notificationService, userInfoRepository);

        var update = TelegramFixtures.textUpdate(1L, "/add-account-ogs");

        assertDoesNotThrow(() -> handler.executeCommand(update));
        verify(notificationService).sendErrorMessage(any(ReplyWithText.class));
    }

    @Test
    void buidldEndpoint_mustUrlEncodeTheUsername_insteadOfConcatenatingItRaw() throws Exception {
        // Regression test: a raw "...?username=" + username concatenation let a username
        // such as "x&format=xml" inject an extra query parameter into the OGS API call.
        AddOgsCommandHandler handler = new AddOgsCommandHandler(
                mock(TelegramChatRepository.class), mock(NotificationService.class), mock(UserInfoRepository.class));

        Method buidldEndpoint = AddOgsCommandHandler.class.getDeclaredMethod("buidldEndpoint", String.class);
        buidldEndpoint.setAccessible(true);

        String endpoint = (String) buidldEndpoint.invoke(handler, "x&format=xml");

        assertEquals("https://online-go.com/api/v1/players/?username=x%26format%3Dxml", endpoint,
                "'&' and '=' inside the username must be percent-encoded, not treated as query syntax");
    }
}
