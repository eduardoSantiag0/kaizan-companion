package io.github.eduardosantiag0.kaizan_companion.infra.security.services;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.OAuthConnectionEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.OAuthConnectionRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GoogleTokenServiceTest {

    private static final String KEY_32_ASCII_CHARS = "01234567890123456789012345678901";

    @Test
    void getValidAccessToken_onCacheHit_mustReturnTheDecryptedPlaintextToken_notTheCiphertext() throws Exception {
        EncryptionService encryptionService = new EncryptionService(KEY_32_ASCII_CHARS);

        TelegramChatRepository chatRepository = mock(TelegramChatRepository.class);
        OAuthConnectionRepository connRepository = mock(OAuthConnectionRepository.class);

        TelegramChatEntity chat = new TelegramChatEntity(1L, "Tester", "tester", "private", false);
        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));

        String plaintextToken = "ya29.real-google-access-token";
        OAuthConnectionEntity conn = new OAuthConnectionEntity(
                chat, "google", "sub-id", Instant.now().plusSeconds(3600));
        conn.setAccessToken(encryptionService.encrypt(plaintextToken));
        when(connRepository.findByChat(chat)).thenReturn(Optional.of(conn));

        GoogleTokenService service = new GoogleTokenService(connRepository, chatRepository, encryptionService);

        String result = service.getValidAccessToken(1L);

        assertEquals(plaintextToken, result,
                "getValidAccessToken must return the decrypted token, ready to use as a bearer token");
        assertNotEquals(conn.getAccessToken(), result,
                "the raw ciphertext stored at rest must never be handed back to a caller");
    }
}
