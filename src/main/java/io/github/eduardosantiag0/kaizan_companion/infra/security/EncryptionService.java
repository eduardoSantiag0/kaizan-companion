package io.github.eduardosantiag0.kaizan_companion.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";

//    Constructs a secret key from the given byte array,
//    using the first len bytes of key , starting at offset inclusive.
    private final SecretKeySpec secretKey;

    public EncryptionService(
            @Value("${app.security.encryption-key}")
            String key)
    {
        if (key == null || key.length() != 32) {
            throw new IllegalArgumentException("No crypt key found");
        }
        this.secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
    }

    public String encrypt(String data) {
        if (data == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypt token", e);
        }
    }

    public String decrypt(String encryptedData) {
        if (encryptedData == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypt token", e);
        }
    }

}
