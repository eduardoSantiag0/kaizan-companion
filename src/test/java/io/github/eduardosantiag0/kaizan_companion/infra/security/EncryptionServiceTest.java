package io.github.eduardosantiag0.kaizan_companion.infra.security;

import io.github.eduardosantiag0.kaizan_companion.infra.security.services.EncryptionService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EncryptionServiceTest {

    private static final String KEY_32_ASCII_CHARS = "01234567890123456789012345678901";

    @Test
    void encrypt_mustNotProduceIdenticalCiphertextBlocksForIdenticalPlaintextBlocks() {
        EncryptionService service = new EncryptionService(KEY_32_ASCII_CHARS);

        String repeatedBlock = "A".repeat(16);
        String plaintext = repeatedBlock + repeatedBlock;

        byte[] cipherBytes = Base64.getDecoder().decode(service.encrypt(plaintext));
        byte[] firstBlock = Arrays.copyOfRange(cipherBytes, 0, 16);
        byte[] secondBlock = Arrays.copyOfRange(cipherBytes, 16, 32);

        assertFalse(Arrays.equals(firstBlock, secondBlock),
                "two identical plaintext blocks produced identical ciphertext blocks: "
                        + "an IV/nonce is required so repeated content is not leaked");
    }

    @Test
    void encrypt_mustNotProduceTheSameCiphertext_forTheSamePlaintextTwice() {
        EncryptionService service = new EncryptionService(KEY_32_ASCII_CHARS);

        String first = service.encrypt("a-google-access-token");
        String second = service.encrypt("a-google-access-token");

        assertFalse(first.equals(second),
                "encrypting the same plaintext twice produced the same ciphertext: "
                        + "a random IV/nonce per call is required for semantic security");
    }

    @Test
    void constructor_mustRejectAKeyThatIsNotActually32BytesLong() {
        String deceptiveKey = "a".repeat(31) + "é";

        assertThrows(IllegalArgumentException.class, () -> new EncryptionService(deceptiveKey),
                "the key's actual byte length was never checked, only its character count");
    }
}
