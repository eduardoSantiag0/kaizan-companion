package io.github.eduardosantiag0.kaizan_companion.infra.auth;

import io.github.eduardosantiag0.kaizan_companion.domain.repositories.OAuthConnectionRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.infra.security.services.EncryptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoogleAuthSuccesHandlerTest {

    @Test
    void onAuthenticationSuccess_mustSendA400_insteadOfThrowing_whenSessionHasNoChatId() throws Exception {
        TelegramChatRepository chatRepository = mock(TelegramChatRepository.class);
        OAuthConnectionRepository connectionRepository = mock(OAuthConnectionRepository.class);
        OAuth2AuthorizedClientService clientService = mock(OAuth2AuthorizedClientService.class);
        EncryptionService encryptionService = mock(EncryptionService.class);

        GoogleAuthSuccesHandler handler = new GoogleAuthSuccesHandler(
                chatRepository, connectionRepository, clientService, encryptionService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        Authentication authentication = mock(Authentication.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("telegramChatId")).thenReturn(null);

        assertDoesNotThrow(() -> handler.onAuthenticationSuccess(request, response, authentication));
        verify(response).sendError(anyInt(), any());
    }
}
