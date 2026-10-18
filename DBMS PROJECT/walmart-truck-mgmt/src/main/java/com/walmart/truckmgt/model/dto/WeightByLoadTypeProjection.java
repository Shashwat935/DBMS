package com.walmart.truckmgt.model.dto;

/**
 * Interface projection for weight dispatched per load type.
 */
public interface WeightByLoadTypeProjection {
    String getLoadType();
    Long getDispatchCount();
    Double getTotalWeight();
}
