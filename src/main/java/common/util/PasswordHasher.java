package common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

//Утилита для хэширования паролей алгоритмом SHA-384

public class PasswordHasher {

    private static final String ALGORITHM = "SHA-384";

    private PasswordHasher() {
    }

    //Хэширует пароль с использованием SHA-384
    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8)
            );
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Алгоритм " + ALGORITHM + " не доступен", e);
        }
    }

    //Преобразует массив байт в 16-ую строку
    private static String bytesToHex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}