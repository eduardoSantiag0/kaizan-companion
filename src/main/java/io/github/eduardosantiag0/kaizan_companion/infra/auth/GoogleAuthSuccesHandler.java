package io.github.eduardosantiag0.kaizan_companion.infra.auth;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.OAuthConnectionEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.OAuthConnectionRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.infra.security.services.EncryptionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

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

        if (session == null || session.getAttribute("telegramChatId") == null) {
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
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Kaizen Companion — Google Drive conectado</title>
            </head>

            <body style="
                margin: 0;
                padding: 40px 16px;
                background-color: #f5f5f5;
                font-family: Arial, Helvetica, sans-serif;
                color: #222222;
            ">

            <table role="presentation" align="center" cellpadding="0" cellspacing="0"
                   style="
                       width: 100%;
                       max-width: 560px;
                       background-color: #ffffff;
                       border: 1px solid #e8e8e8;
                       border-radius: 12px;
                   ">

                <tr>
                    <td style="padding: 36px 40px; text-align: center;">

                        <h1 style="
                            margin: 0 0 16px;
                            font-size: 26px;
                            font-weight: 700;
                            letter-spacing: -0.5px;
                            color: #171717;
                        ">
                            Google Drive conectado com sucesso!
                        </h1>

                        <p style="
                            font-size: 15px;
                            line-height: 1.7;
                            color: #555555;
                            margin: 0;
                        ">
                            Sua conta do Google foi vinculada ao Kaizen Companion.
                            <br>
                            Você já pode voltar ao Telegram.
                        </p>

                    </td>
                </tr>
            </table>

            <!-- Footer -->
            <p style="
                text-align: center;
                font-size: 12px;
                color: #999999;
                margin-top: 24px;
                line-height: 1.6;
            ">
                ⛩ KAIZEN COMPANION
                <br>
            </p>

            </body>
            </html>
            """;
    }
}
