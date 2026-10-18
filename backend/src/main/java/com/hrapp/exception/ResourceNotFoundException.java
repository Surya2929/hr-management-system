package com.hrapp.exception;

/** Thrown when a record (employee, job, candidate...) does not exist. Mapped to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
