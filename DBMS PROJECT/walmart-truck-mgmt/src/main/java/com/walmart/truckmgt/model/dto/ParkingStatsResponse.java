package com.walmart.truckmgt.model.dto;

import lombok.*;

/**
 * Summary stats for the parking area.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParkingStatsResponse {
    private long totalSlots;
    private long occupiedSlots;
    private long freeSlots;
}
