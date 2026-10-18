package com.walmart.truckmgt.model.dto;

/**
 * Interface projection for daily dispatch summary.
 */
public interface DailySummaryProjection {
    String getDispatchDate();
    Long getDispatchCount();
    Double getTotalWeight();
}
