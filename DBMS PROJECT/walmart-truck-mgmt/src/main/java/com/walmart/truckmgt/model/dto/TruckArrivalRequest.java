package com.walmart.truckmgt.model.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for registering a new truck arrival.
 * All fields are validated; size_class is computed automatically.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TruckArrivalRequest {

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotBlank(message = "Driver name is required")
    private String driverName;

    @NotBlank(message = "Origin city is required")
    private String originCity;

    @Positive(message = "Distance must be positive")
    private Double locationDistance;

    @Positive(message = "Max weight capacity must be positive")
    private Double maxWeightCapacity;

    /** ID of the load type last carried (null = clean/new truck) */
    private Long previousLoadTypeId;
}
