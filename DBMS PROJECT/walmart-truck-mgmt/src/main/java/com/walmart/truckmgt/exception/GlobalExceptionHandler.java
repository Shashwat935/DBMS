package com.walmart.truckmgt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler – catches all custom exceptions and validation errors,
 * returns a consistent JSON error body: { timestamp, status, error, message }.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---- Custom business exceptions ----

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ParkingFullException.class)
    public ResponseEntity<Map<String, Object>> handleParkingFull(ParkingFullException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(NoGateAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleNoGate(NoGateAvailableException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(NoTruckAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleNoTruck(NoTruckAvailableException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ---- Validation errors (from @Valid) ----

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return buildResponse(HttpStatus.BAD_REQUEST, msg);
    }

    // ---- Catch-all ----

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage());
    }

    /** Build a uniform error response body */
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
