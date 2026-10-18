package com.walmart.truckmgt.exception;

/** Thrown when no truck meets the criteria for a dispatch request. */
public class NoTruckAvailableException extends RuntimeException {
    public NoTruckAvailableException(String message) {
        super(message);
    }
}
