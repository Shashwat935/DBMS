package com.walmart.truckmgt.model.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * Dashboard summary DTO – counts and totals shown on the home page.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardSummary {
    private long trucksInParking;
    private long trucksInCleaning;
    private long trucksAtGate;
    private long trucksDispatched;
    private long gatesIdle;
    private long gatesLoading;
    private long gatesUnloading;
    private long freeSlots;
    private long pendingRequests;
    private BigDecimal totalCleaningCost;
}
