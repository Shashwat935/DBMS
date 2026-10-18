package com.walmart.truckmgt.model.entity;

/**
 * Possible statuses for a dispatch operation.
 * PENDING      – request created, no truck assigned yet
 * ASSIGNED     – truck selected, transient before cleaning/gate
 * IN_CLEANING  – truck is being cleaned before it goes to the gate
 * IN_PROGRESS  – truck is at the gate; loading/unloading underway
 * COMPLETED    – operation finished successfully
 * CANCELLED    – operation was cancelled
 */
public enum DispatchStatus {
    PENDING,
    ASSIGNED,
    IN_CLEANING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
