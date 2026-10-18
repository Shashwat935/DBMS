package com.walmart.truckmgt.service;

import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.model.entity.*;
import com.walmart.truckmgt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ReportService – wraps the aggregate/report queries from various repositories.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final DispatchOperationRepository dispatchRepo;
    private final WarehouseGateRepository gateRepo;
    private final TruckRepository truckRepo;
    private final CleaningLogRepository cleaningRepo;
    private final CompatibilityRuleRepository ruleRepo;
    private final LoadTypeRepository loadTypeRepo;

    /** AGGREGATE 9: Weight dispatched per load type */
    public List<WeightByLoadTypeProjection> getWeightByLoadType() {
        return dispatchRepo.findWeightByLoadType();
    }

    /** AGGREGATE 10: Gate performance (dispatches per gate, avg duration) */
    public List<GatePerformanceProjection> getGatePerformance() {
        return gateRepo.findGatePerformance();
    }

    /** AGGREGATE 11: Trucks per size class in parking */
    public List<SizeClassCountProjection> getSizeClassCounts() {
        return truckRepo.countBySizeClassInParking();
    }

    /** AGGREGATE 12: Daily dispatch summary */
    public List<DailySummaryProjection> getDailySummary() {
        return dispatchRepo.findDailySummary();
    }

    /** UNION 7: Active trucks (at gate + in cleaning) */
    public List<TruckLedgerProjection> getActiveTrucks() {
        return truckRepo.findActiveTrucks();
    }

    /** All compatibility rules */
    public List<CompatibilityResponse> getAllRules() {
        return ruleRepo.findAll().stream()
                .map(r -> CompatibilityResponse.builder()
                        .id(r.getId())
                        .fromLoadType(r.getFromLoadType().getName())
                        .toLoadType(r.getToLoadType().getName())
                        .cleaningRequired(r.getCleaningRequired())
                        .cleaningCost(r.getCleaningCost())
                        .cleaningMinutes(r.getCleaningMinutes())
                        .build())
                .collect(Collectors.toList());
    }

    /** Check compatibility between two load types */
    public CompatibilityResponse checkCompatibility(Long fromId, Long toId) {
        return ruleRepo.findByFromAndTo(fromId, toId)
                .map(r -> CompatibilityResponse.builder()
                        .id(r.getId())
                        .fromLoadType(r.getFromLoadType().getName())
                        .toLoadType(r.getToLoadType().getName())
                        .cleaningRequired(r.getCleaningRequired())
                        .cleaningCost(r.getCleaningCost())
                        .cleaningMinutes(r.getCleaningMinutes())
                        .build())
                .orElse(CompatibilityResponse.builder()
                        .fromLoadType(fromId.toString())
                        .toLoadType(toId.toString())
                        .cleaningRequired(false)
                        .cleaningMinutes(0)
                        .build());
    }

    /** Get all load types */
    public List<LoadType> getAllLoadTypes() {
        return loadTypeRepo.findAll();
    }
}
