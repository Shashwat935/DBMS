package com.walmart.truckmgt.model.dto;

/**
 * Interface projection for trucks per size class count.
 */
public interface SizeClassCountProjection {
    String getSizeClass();
    Long getTruckCount();
}
