package com.hrapp.exception;

/**
 * A business-rule error caused by the user's request, e.g. "Already checked out today".
 * Mapped to HTTP 400 and its message IS shown to the user, so keep messages friendly.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
