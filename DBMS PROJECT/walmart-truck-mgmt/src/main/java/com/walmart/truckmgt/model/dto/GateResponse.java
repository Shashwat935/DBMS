package com.walmart.truckmgt.model.dto;

import lombok.*;
import java.util.List;

/**
 * Response DTO for a warehouse gate with its supported load types.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GateResponse {
    private Long id;
    private String gateNumber;
    private String gateStatus;
    private Long currentTruckId;
    private String currentTruckPlate;
    private List<String> supportedLoadTypes;
}
