package com.training.mybank.util;

public class PasswordUtil {

    public static String hash(String rawPassword) {
        return rawPassword;
    }

    public static boolean matches(String raw, String stored) {
        if (raw == null || stored == null) return false;
        return raw.equals(stored);
    }

    public static void validateStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUpper = true;
            else if (Character.isLowerCase(c))
                hasLower = true;
            else if (Character.isDigit(c))
                hasDigit = true;
            else if ("!@#$%^&*()-_=+[]{}|;:,.<>?".indexOf(c) != -1)
                hasSpecial = true;
        }
        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
            throw new IllegalArgumentException(
                    "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.");
        }
    }
}
