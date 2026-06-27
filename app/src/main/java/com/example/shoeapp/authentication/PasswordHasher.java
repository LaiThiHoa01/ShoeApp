package com.example.shoeapp.authentication;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class PasswordHasher {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String hash(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        String saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP);
        String hashBase64 = sha256(password, saltBase64);

        return saltBase64 + ":" + hashBase64;
    }

    public static boolean verify(String password, String storedHash) {
        if (storedHash == null) return false;

        if (!storedHash.contains(":")) {
            return storedHash.equals(password);
        }

        String[] parts = storedHash.split(":");
        if (parts.length != 2) return false;

        String saltBase64 = parts[0];
        String expectedHash = parts[1];
        String actualHash = sha256(password, saltBase64);

        return expectedHash.equals(actualHash);
    }

    private static String sha256(String password, String saltBase64) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = saltBase64 + password;
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException("Không thể mã hóa mật khẩu", e);
        }
    }
}