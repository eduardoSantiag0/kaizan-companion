package io.github.eduardosantiag0.kaizan_companion.infra.security.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

//    Constructs a secret key from the given byte array,
//    using the first len bytes of key , starting at offset inclusive.
    private final SecretKeySpec secretKey;

    private static final int TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;


    public EncryptionService(
            @Value("${app.security.encryption-key}")
            String key)
    {
        if (key == null || key.getBytes(StandardCharsets.UTF_8).length != 32) {
            throw new IllegalArgumentException("No crypt key found or size is not 32 bytes");
        }

        this.secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
    }

    // Generate Nounc
    public static byte[] generateNonce() {
        byte[] nonce = new byte[IV_LENGTH_BYTES];
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonce);
        return nonce;
    }


    public String encrypt(String data) {
        if (data == null) return null;
        try {
            byte[] iv = generateNonce();

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] encryptedBytes = cipher.doFinal(data.getBytes());

            byte[] encryptedWithNonce = ByteBuffer.allocate(iv.length + encryptedBytes.length)
                    .put(iv)
                    .put(encryptedBytes)
                    .array();

            return Base64.getEncoder().encodeToString(encryptedWithNonce);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypt token", e);
        }
    }

    public String decrypt(String encryptedData) {
        if (encryptedData == null) return null;

        try {
            byte[] combinedPayload = Base64.getDecoder().decode(encryptedData);
            ByteBuffer buffer = ByteBuffer.wrap(combinedPayload);

            byte[] iv = new byte[IV_LENGTH_BYTES];
            buffer.get(iv);

            byte[] passwordEncrypted = new byte[buffer.remaining()];
            buffer.get(passwordEncrypted);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] decryptedBytes = cipher.doFinal(passwordEncrypted);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Error decrypt token", e);
        }
    }

}
