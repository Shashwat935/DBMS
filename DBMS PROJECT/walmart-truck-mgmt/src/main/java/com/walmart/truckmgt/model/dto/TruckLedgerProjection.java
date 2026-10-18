package com.walmart.truckmgt.model.dto;

import java.time.LocalDateTime;

/**
 * Interface projection for UNION queries (combined ledger, active trucks).
 * Column aliases in the native query must match the getter names exactly.
 */
public interface TruckLedgerProjection {
    Long getTruckId();
    String getLicensePlate();
    String getCurrentStatus();
    Double getLocationDistance();
    LocalDateTime getArrivalTimestamp();
    String getSource();
}
