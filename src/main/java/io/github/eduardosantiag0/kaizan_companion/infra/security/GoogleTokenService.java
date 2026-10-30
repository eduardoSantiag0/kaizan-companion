package io.github.eduardosantiag0.kaizan_companion.infra.security;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.OAuthConnectionEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.OAuthConnectionRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.infra.auth.GoogleTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;

@Service
public class GoogleTokenService {

    private final OAuthConnectionRepository connRepository;
    private final TelegramChatRepository chatRepository;


    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private final String RENEW_ACESS_TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    public GoogleTokenService(OAuthConnectionRepository repository, TelegramChatRepository chatRepository) throws GeneralSecurityException, IOException {
        this.connRepository = repository;
        this.chatRepository = chatRepository;
    }


    public String getValidAccessToken(Long chatId) {

        TelegramChatEntity chatEntity = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        OAuthConnectionEntity conn = connRepository.findByChat(chatEntity)
                .orElseThrow(() -> new RuntimeException("Not token registered"));

        String token = conn.getAccessToken();
        if (token != null
                && !token.isBlank()
                && conn.getExpiresAt() != null
                && Instant.now().isBefore(conn.getExpiresAt())) {
            return token;
        }

        return refreshAccessToken(conn);

    }


    private String refreshAccessToken(OAuthConnectionEntity conn) {
        String refreshToken = conn.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Google account must be reconnected");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        WebClient webClient = WebClient.create();

        GoogleTokenResponse response = webClient.post()
                .uri(RENEW_ACESS_TOKEN_ENDPOINT)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .block();

        if (response == null || response.accessToken() == null) {
            throw new RuntimeException("Failed to refresh Google token");
        }

        conn.setAccessToken(response.accessToken());

        conn.setExpiresAt(
                Instant.now().plusSeconds(
                        Math.max(0, response.expiresIn() - 60)
                )
        );

        connRepository.save(conn);

        return response.accessToken();

    }

}
