package com.walmart.truckmgt.service;

import com.walmart.truckmgt.exception.ResourceNotFoundException;
import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.model.entity.*;
import com.walmart.truckmgt.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CleaningService – handles cleaning log queries and the "complete cleaning" action.
 * When cleaning finishes, the truck transitions from IN_CLEANING → AT_GATE,
 * and the dispatch from IN_CLEANING → IN_PROGRESS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CleaningService {

    private final CleaningLogRepository cleaningRepo;
    private final TruckRepository truckRepo;
    private final DispatchOperationRepository dispatchRepo;
    private final CompatibilityRuleRepository ruleRepo;

    /**
     * Complete a cleaning operation.
     * Sets end_time, marks status COMPLETED, moves truck to AT_GATE,
     * and sets dispatch status to IN_PROGRESS with started_at = now.
     */
    @Transactional
    public CleaningLogResponse completeCleaning(Long cleaningLogId) {
        CleaningLog cl = cleaningRepo.findById(cleaningLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Cleaning log not found: " + cleaningLogId));

        if ("COMPLETED".equals(cl.getStatus())) {
            throw new IllegalStateException("Cleaning #" + cleaningLogId + " is already COMPLETED");
        }

        // Mark cleaning as done
        cl.setEndTime(LocalDateTime.now());
        cl.setStatus("COMPLETED");
        cleaningRepo.save(cl);

        // Move truck from IN_CLEANING → AT_GATE
        Truck truck = cl.getTruck();
        truck.setCurrentStatus(TruckStatus.AT_GATE);
        truckRepo.save(truck);

        // Move dispatch from IN_CLEANING → IN_PROGRESS
        if (cl.getDispatch() != null) {
            DispatchOperation dispatch = cl.getDispatch();
            dispatch.setStatus(DispatchStatus.IN_PROGRESS);
            dispatch.setStartedAt(LocalDateTime.now());
            dispatchRepo.save(dispatch);
        }

        log.info("Cleaning #{} completed for truck {}", cleaningLogId, truck.getLicensePlate());
        return toResponse(cl);
    }

    /** Get all currently in-progress cleaning operations */
    public List<CleaningLogResponse> findActive() {
        return cleaningRepo.findByStatus("IN_PROGRESS").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Get all cleaning logs (history) */
    public List<CleaningLogResponse> findAll() {
        return cleaningRepo.findAllWithDetails().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Cleaning cost by truck with HAVING filter */
    public List<CleaningCostProjection> getCostByTruck(double minCost) {
        return cleaningRepo.findCostByTruckAboveMin(minCost);
    }

    /** Total cleaning cost across all logs */
    public BigDecimal getTotalCleaningCost() {
        return cleaningRepo.totalCleaningCost();
    }

    // ---- Helper: convert entity to DTO ----
    private CleaningLogResponse toResponse(CleaningLog cl) {
        // Try to get cleaning minutes from the compatibility rule
        Integer cleaningMinutes = null;
        if (cl.getTruck() != null && cl.getTruck().getPreviousLoadType() != null
                && cl.getDispatch() != null && cl.getDispatch().getLoadType() != null) {
            cleaningMinutes = ruleRepo.findByFromAndTo(
                    cl.getTruck().getPreviousLoadType().getId(),
                    cl.getDispatch().getLoadType().getId())
                    .map(CompatibilityRule::getCleaningMinutes)
                    .orElse(null);
        }

        return CleaningLogResponse.builder()
                .id(cl.getId())
                .truckId(cl.getTruck() != null ? cl.getTruck().getId() : null)
                .licensePlate(cl.getTruck() != null ? cl.getTruck().getLicensePlate() : null)
                .driverName(cl.getTruck() != null ? cl.getTruck().getDriverName() : null)
                .dispatchId(cl.getDispatch() != null ? cl.getDispatch().getId() : null)
                .previousLoadType(cl.getTruck() != null && cl.getTruck().getPreviousLoadType() != null
                        ? cl.getTruck().getPreviousLoadType().getName() : null)
                .startTime(cl.getStartTime())
                .endTime(cl.getEndTime())
                .cost(cl.getCost())
                .status(cl.getStatus())
                .cleaningMinutes(cleaningMinutes)
                .build();
    }
}
