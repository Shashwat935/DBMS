package com.walmart.truckmgt.service;

import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.model.entity.*;
import com.walmart.truckmgt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * DashboardService – aggregates summary data for the main dashboard.
 * Counts of trucks by status, gates by status, free slots, pending requests,
 * and total cleaning cost.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TruckRepository truckRepo;
    private final WarehouseGateRepository gateRepo;
    private final ParkingSlotRepository slotRepo;
    private final DispatchOperationRepository dispatchRepo;
    private final CleaningLogRepository cleaningRepo;

    /** Build dashboard summary DTO */
    public DashboardSummary getSummary() {
        return DashboardSummary.builder()
                .trucksInParking(truckRepo.countByCurrentStatus(TruckStatus.IN_PARKING))
                .trucksInCleaning(truckRepo.countByCurrentStatus(TruckStatus.IN_CLEANING))
                .trucksAtGate(truckRepo.countByCurrentStatus(TruckStatus.AT_GATE))
                .trucksDispatched(truckRepo.countByCurrentStatus(TruckStatus.DISPATCHED))
                .gatesIdle(gateRepo.countByGateStatus(GateStatus.IDLE))
                .gatesLoading(gateRepo.countByGateStatus(GateStatus.LOADING))
                .gatesUnloading(gateRepo.countByGateStatus(GateStatus.UNLOADING))
                .freeSlots(slotRepo.countByIsOccupied(false))
                .pendingRequests(dispatchRepo.countByStatus(DispatchStatus.PENDING))
                .totalCleaningCost(cleaningRepo.totalCleaningCost())
                .build();
    }
}
