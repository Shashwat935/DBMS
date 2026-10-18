package com.walmart.truckmgt.exception;

/** Thrown when all 40 parking slots are occupied. */
public class ParkingFullException extends RuntimeException {
    public ParkingFullException(String message) {
        super(message);
    }
}
