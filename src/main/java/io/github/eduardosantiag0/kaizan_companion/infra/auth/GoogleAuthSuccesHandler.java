package io.github.eduardosantiag0.kaizan_companion.infra.auth;

import com.google.auth.oauth2.TokenStore;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.OAuthConnectionEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.OAuthConnectionRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.infra.security.EncryptionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
@Slf4j
public class GoogleAuthSuccesHandler implements AuthenticationSuccessHandler {

    private final TelegramChatRepository chatRepository;
    private final OAuthConnectionRepository oAuthConnectionRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final EncryptionService encryptionService;

    public GoogleAuthSuccesHandler(TelegramChatRepository chatRepository,
                                   OAuthConnectionRepository oAuthConnectionRepository,
                                   OAuth2AuthorizedClientService authorizedClientService, EncryptionService encryptionService) {
        this.chatRepository = chatRepository;
        this.oAuthConnectionRepository = oAuthConnectionRepository;
        this.authorizedClientService = authorizedClientService;
        this.encryptionService = encryptionService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException
    {
        HttpSession session = request.getSession();

        if (session == null) {
            response.sendError(400, "Missing Telegram connection");
            return;
        }

        Long chatId = (Long) session.getAttribute("telegramChatId");

        log.debug("No Filtro de Security:  {}", chatId.toString());

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

            OAuth2User oauth2User = oauthToken.getPrincipal();

            // Identifica provedor
            String provider = oauthToken.getAuthorizedClientRegistrationId();

            // ID único
            String providerId = oauth2User.getAttribute("sub");

            log.debug("Provider: {} e ProviderId: {}", provider, providerId);

            TelegramChatEntity chatEntity = chatRepository.findById(chatId)
                    //TODO Criar TelegramChatNotFoundException(chatId));
                    .orElseThrow(() -> new RuntimeException("Chat not found"));


            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );

            Instant expiresAt = authorizedClient.getAccessToken().getExpiresAt();

            OAuthConnectionEntity authConnectionEntity = new OAuthConnectionEntity(
                    chatEntity, provider, providerId, expiresAt
            );

            String accessTokenValue = authorizedClient.getAccessToken().getTokenValue();

            log.debug("Token encontrado: {}\n", accessTokenValue);


            String refreshTokenValue = null;
            if (authorizedClient.getRefreshToken() != null) {
                refreshTokenValue = authorizedClient.getRefreshToken().getTokenValue();
            }

            String encryptedAccess =encryptionService.encrypt(accessTokenValue);

            log.debug(encryptedAccess);

            authConnectionEntity.setAccessToken(encryptedAccess);

            if (refreshTokenValue != null) {
                authConnectionEntity.setRefreshToken(encryptionService.encrypt(refreshTokenValue));
            }

            log.debug("Salvando a entidade...\n");

            oAuthConnectionRepository.save(authConnectionEntity);

            chatEntity.setDriveLinked(true);
            chatRepository.save(chatEntity);

            session.removeAttribute("telegramChatId");

            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(htmlRedirect());

        }

    }

    private String htmlRedirect() {
        return """
            <html>
                <body>
                    <h2>Google Drive conectado com sucesso!</h2>
                    <p>Você já pode voltar ao Telegram.</p>
                </body>
            </html>
            """;
    }
}
