package com.hrapp.exception;

/** The AI (Groq) call failed or returned something unusable. Mapped to HTTP 503 with a friendly message. */
public class AiServiceException extends RuntimeException {
    public AiServiceException(String message) {
        super(message);
    }
}
