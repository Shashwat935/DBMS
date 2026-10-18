package com.walmart.truckmgt.model.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for creating a dispatch operation.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DispatchRequest {

    @NotNull(message = "Load type ID is required")
    private Long loadTypeId;

    @NotNull(message = "Load weight is required")
    @Positive(message = "Load weight must be positive")
    private Double loadWeight;

    @NotNull(message = "Operation type is required (LOADING or UNLOADING)")
    private String operationType;

    @PositiveOrZero(message = "Destination distance cannot be negative")
    private Double destinationDistance = 0.0;

    private Boolean isEmergency = false;
}
