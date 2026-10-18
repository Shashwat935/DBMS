package com.walmart.truckmgt.model.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Response DTO for dispatch operations, includes computed progress %.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DispatchResponse {
    private Long id;
    private Long truckId;
    private String truckPlate;
    private String driverName;
    private Long gateId;
    private String gateNumber;
    private String loadTypeName;
    private Long loadTypeId;
    private String operationType;
    private Double loadWeight;
    private Double destinationDistance;
    private Boolean isEmergency;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private Integer expectedMinutes;
    private LocalDateTime completedAt;
    private Double progressPercent;   // computed in service
}
