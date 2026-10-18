package com.walmart.truckmgt.model.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for cleaning log entries.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CleaningLogResponse {
    private Long id;
    private Long truckId;
    private String licensePlate;
    private String driverName;
    private Long dispatchId;
    private String previousLoadType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal cost;
    private String status;
    private Integer cleaningMinutes;  // from the compatibility rule
}
