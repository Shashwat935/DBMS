package com.walmart.truckmgt.model.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * Response DTO for the compatibility matrix.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompatibilityResponse {
    private Long id;
    private String fromLoadType;
    private String toLoadType;
    private Boolean cleaningRequired;
    private BigDecimal cleaningCost;
    private Integer cleaningMinutes;
}
