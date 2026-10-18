package com.walmart.truckmgt.model.dto;

/**
 * Interface projection for gate performance (dispatches per gate).
 */
public interface GatePerformanceProjection {
    String getGateNumber();
    Long getTotalDispatches();
    Double getAvgDurationMin();
}
