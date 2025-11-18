package com.project.bankrest.Security;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String SECRET = "12345678901234567890123456789012";
    private SecretKeySpec secretKey;

    @PostConstruct
    public void init() {
        secretKey = new SecretKeySpec(SECRET.getBytes(), ALGORITHM);
    }

    @SneakyThrows
    public String encrypt(String str) {
        if (str == null) return null;

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return Base64.getEncoder().encodeToString(cipher.doFinal(str.getBytes()));
    }

    @SneakyThrows
    public String decrypt(String encrypted) {
        if (encrypted == null) return null;

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
    }
}
