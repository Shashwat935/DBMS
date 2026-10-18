package com.walmart.truckmgt.model.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Response DTO for truck data (never expose the entity directly).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TruckResponse {
    private Long id;
    private String licensePlate;
    private String driverName;
    private String originCity;
    private Double locationDistance;
    private Double maxWeightCapacity;
    private String previousLoadType;   // name, not entity
    private Long previousLoadTypeId;
    private String currentStatus;
    private LocalDateTime arrivalTimestamp;
    private String sizeClass;
    private String slotNumber;         // parking slot if parked
}
