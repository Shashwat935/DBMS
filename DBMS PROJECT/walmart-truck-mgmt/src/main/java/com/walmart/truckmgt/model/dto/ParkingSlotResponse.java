package com.walmart.truckmgt.model.dto;

import lombok.*;

/**
 * Response DTO for a single parking slot.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParkingSlotResponse {
    private Long id;
    private String slotNumber;
    private String rowLabel;
    private Integer colNumber;
    private Boolean isOccupied;
    private Long truckId;
    private String licensePlate;
    private String driverName;
    private String sizeClass;
}
