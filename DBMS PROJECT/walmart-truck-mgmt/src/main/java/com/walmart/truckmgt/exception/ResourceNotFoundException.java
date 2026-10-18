package com.walmart.truckmgt.exception;

/**
 * Thrown when a requested resource (truck, gate, etc.) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
