package com.walmart.truckmgt.model.entity;

/**
 * Possible statuses for a warehouse gate.
 * IDLE      – no truck at the gate
 * LOADING   – a truck is being loaded with cargo
 * UNLOADING – a truck is being unloaded
 */
public enum GateStatus {
    IDLE,
    LOADING,
    UNLOADING
}
