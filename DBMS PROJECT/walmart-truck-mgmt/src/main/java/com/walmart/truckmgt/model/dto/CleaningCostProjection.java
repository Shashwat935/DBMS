package com.walmart.truckmgt.model.dto;

import java.math.BigDecimal;

/**
 * Interface projection for cleaning cost per truck (aggregate query).
 */
public interface CleaningCostProjection {
    String getLicensePlate();
    String getDriverName();
    Long getCleaningCount();
    BigDecimal getTotalCost();
}
