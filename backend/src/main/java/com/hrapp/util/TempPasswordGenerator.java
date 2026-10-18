package com.hrapp.util;

import java.security.SecureRandom;

/**
 * Creates the one-time password HR gives to a new employee.
 * Uses SecureRandom (not Math.random) and skips look-alike characters (0/O, 1/l/I).
 */
public class TempPasswordGenerator {

    private static final String LETTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";
    private static final String DIGITS  = "23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private TempPasswordGenerator() {}

    // e.g. "kT7wPq4xRm" - 10 characters, always contains letters and digits
    public static String generate() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            String pool = (i % 3 == 2) ? DIGITS : LETTERS; // every 3rd char is a digit
            sb.append(pool.charAt(RANDOM.nextInt(pool.length())));
        }
        return sb.toString();
    }
}
