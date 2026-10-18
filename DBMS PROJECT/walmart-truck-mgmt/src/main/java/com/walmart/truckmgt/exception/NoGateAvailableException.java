package com.walmart.truckmgt.exception;

/** Thrown when no suitable gate is available for a dispatch. */
public class NoGateAvailableException extends RuntimeException {
    public NoGateAvailableException(String message) {
        super(message);
    }
}
