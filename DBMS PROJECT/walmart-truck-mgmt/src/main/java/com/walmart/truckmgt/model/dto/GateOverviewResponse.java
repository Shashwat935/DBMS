package com.walmart.truckmgt.model.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Rich gate overview DTO with truck, dispatch, and load type info.
 * Used by the gate overview JOIN query.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GateOverviewResponse {
    private Long gateId;
    private String gateNumber;
    private String gateStatus;
    private Long truckId;
    private String licensePlate;
    private String driverName;
    private Long dispatchId;
    private String operationType;
    private Double loadWeight;
    private String dispatchStatus;
    private LocalDateTime startedAt;
    private Integer expectedMinutes;
    private String loadTypeName;
    private String loadTypeCategory;
    private Double progressPercent;
    private Boolean isCleaning;
}
