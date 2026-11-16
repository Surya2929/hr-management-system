package com.hrapp.util;

import com.hrapp.exception.BadRequestException;

/**
 * Small helpers to validate values that arrive as a Map (not a @Valid DTO).
 * Each one throws a friendly 400 error that names the field.
 */
public class InputCheck {

    private InputCheck() {}

    // Required text: not null/blank, trimmed, at most maxLength characters
    public static String requiredText(Object value, String field, int maxLength) {
        String text = value == null ? "" : value.toString().trim();
        if (text.isEmpty()) {
            throw new BadRequestException(field + " is required");
        }
        return maxLength(text, field, maxLength);
    }

    // Optional text: null/blank becomes null, otherwise trimmed and length-checked
    public static String optionalText(Object value, String field, int maxLength) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        return maxLength(value.toString().trim(), field, maxLength);
    }

    private static String maxLength(String text, String field, int maxLength) {
        if (text.length() > maxLength) {
            throw new BadRequestException(field + " must be at most " + maxLength + " characters");
        }
        return text;
    }
}
