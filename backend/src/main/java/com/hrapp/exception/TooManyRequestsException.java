package com.hrapp.exception;

/** Too many login / activation / reset attempts. Mapped to HTTP 429. */
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
