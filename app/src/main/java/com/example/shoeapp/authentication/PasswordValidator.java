package com.example.shoeapp.authentication;

import java.util.Locale;

public final class PasswordValidator {
    private PasswordValidator() {
    }

    public static boolean isStrong(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasUppercase = !password.equals(password.toLowerCase(Locale.ROOT));
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        return hasUppercase && hasNumber && hasSpecial;
    }

    public static int getStrength(String password) {
        if (password == null || password.isEmpty()) return 0;

        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++;

        return score;
    }
}