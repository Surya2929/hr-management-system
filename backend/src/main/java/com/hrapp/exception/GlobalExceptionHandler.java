package com.hrapp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns every exception into the same clean JSON shape:
 * { timestamp, status, error, message, details? }
 * Stack traces are only logged on the server, never sent to the client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - @Valid failed on a request body. details = { field: message }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> details.putIfAbsent(err.getField(), err.getDefaultMessage()));

        // "message" keeps the frontend's existing err.response.data.message working
        String firstError = details.entrySet().stream()
                .findFirst()
                .map(e -> e.getKey() + " " + e.getValue())
                .orElse("Invalid request");

        Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, "Validation failed", firstError);
        body.put("details", details);
        return ResponseEntity.badRequest().body(body);
    }

    // 400 - our own business rules (e.g. "Already checked out today"). These messages are written
    // for users, so they are safe to show.
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 400 - bad values from Java/libraries, e.g. an unknown enum name or "abc" as a number.
    // Their messages contain class names, so we show a generic text instead.
    @ExceptionHandler({IllegalArgumentException.class, DateTimeParseException.class})
    public ResponseEntity<Map<String, Object>> handleInvalidValue(RuntimeException ex) {
        log.debug("Invalid value in request: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, "Invalid value in the request (check dates, numbers and options)");
    }

    // 400 - malformed JSON, missing / wrong-type query params, missing upload part
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> handleBadInput(Exception ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Invalid or missing request data");
    }

    // 405 / 415 - wrong HTTP method or content type (used to fall into the 500 handler)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethod(HttpRequestMethodNotSupportedException ex) {
        return buildError(HttpStatus.METHOD_NOT_ALLOWED, "This HTTP method is not supported here");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return buildError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported content type");
    }

    // 429 - too many login / activation / reset attempts
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Map<String, Object>> handleTooMany(TooManyRequestsException ex) {
        return buildError(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    // 503 - AI (Groq) failed or timed out. Message is friendly; details are in the server log.
    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<Map<String, Object>> handleAi(AiServiceException ex) {
        return buildError(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    // 400 - upload bigger than the multipart limit
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleTooLarge(MaxUploadSizeExceededException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "File is too large. Maximum size is 5 MB");
    }

    // 401 - wrong email or password (same message for both, so we don't reveal which one)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    // 403 - correct temporary password, but the account must be activated first.
    // error = "ACTIVATION_REQUIRED" lets the frontend open the activation screen.
    @ExceptionHandler(AccountNotActivatedException.class)
    public ResponseEntity<Map<String, Object>> handleNotActivated(AccountNotActivatedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildBody(HttpStatus.FORBIDDEN, "ACTIVATION_REQUIRED", ex.getMessage()));
    }

    // 403 - logged in but wrong role
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, "Access denied: insufficient permissions");
    }

    // 404 - record not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 404 - unknown URL
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, "Endpoint not found");
    }

    // 409 - DB constraint (duplicate value, or deleting a record that others still use)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return buildError(HttpStatus.CONFLICT,
                "This action conflicts with existing data (duplicate or still in use)");
    }

    // 500 - anything unexpected. Log the details, return a generic message.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again.");
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(buildBody(status, status.getReasonPhrase(), message));
    }

    private Map<String, Object> buildBody(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
