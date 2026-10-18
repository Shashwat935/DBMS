package com.walmart.truckmgt.model.entity;

/**
 * Possible statuses for a truck in the system.
 * IN_PARKING  – parked in a slot, waiting for dispatch
 * IN_CLEANING – being cleaned before gate assignment
 * AT_GATE     – at a warehouse gate, loading/unloading
 * DISPATCHED  – finished and left the warehouse
 */
public enum TruckStatus {
    IN_PARKING,
    IN_CLEANING,
    AT_GATE,
    DISPATCHED
}
