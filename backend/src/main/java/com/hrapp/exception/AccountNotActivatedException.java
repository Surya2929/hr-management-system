package com.hrapp.exception;

/** Login with a temporary password before activation. Mapped to HTTP 403 "ACTIVATION_REQUIRED". */
public class AccountNotActivatedException extends RuntimeException {
    public AccountNotActivatedException(String message) {
        super(message);
    }
}
