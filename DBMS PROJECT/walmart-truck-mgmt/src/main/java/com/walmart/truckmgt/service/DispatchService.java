package com.walmart.truckmgt.service;

import com.walmart.truckmgt.exception.*;
import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.model.entity.*;
import com.walmart.truckmgt.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DispatchService – core business logic for dispatch operations.
 * Handles: creating requests, assigning trucks+gates, cleaning checks,
 *          completing/cancelling operations, and progress calculation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DispatchService {

    private final DispatchOperationRepository dispatchRepo;
    private final TruckRepository truckRepo;
    private final WarehouseGateRepository gateRepo;
    private final LoadTypeRepository loadTypeRepo;
    private final CompatibilityRuleRepository ruleRepo;
    private final CleaningLogRepository cleaningRepo;
    private final ParkingSlotRepository slotRepo;
    private final PriorityService priorityService;

    @Value("${app.simulation.speed-factor:1}")
    private int speedFactor;

    // ---------------------------------------------------------------
    // CREATE a new dispatch request (PENDING)
    // ---------------------------------------------------------------
    @Transactional
    public DispatchResponse createRequest(DispatchRequest req) {
        LoadType loadType = loadTypeRepo.findById(req.getLoadTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Load type not found: " + req.getLoadTypeId()));

        OperationType opType = OperationType.valueOf(req.getOperationType().toUpperCase());

        DispatchOperation dispatch = DispatchOperation.builder()
                .loadType(loadType)
                .operationType(opType)
                .loadWeight(req.getLoadWeight())
                .destinationDistance(req.getDestinationDistance() != null ? req.getDestinationDistance() : 0.0)
                .isEmergency(req.getIsEmergency() != null ? req.getIsEmergency() : false)
                .status(DispatchStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        dispatch = dispatchRepo.save(dispatch);
        return toResponse(dispatch);
    }

    // ---------------------------------------------------------------
    // ASSIGN a single PENDING request
    // ---------------------------------------------------------------
    @Transactional
    public DispatchResponse assignSingle(Long dispatchId) {
        DispatchOperation dispatch = dispatchRepo.findById(dispatchId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found: " + dispatchId));

        if (dispatch.getStatus() != DispatchStatus.PENDING) {
            throw new IllegalStateException("Dispatch #" + dispatchId + " is not PENDING (status: " + dispatch.getStatus() + ")");
        }

        return doAssign(dispatch);
    }

    // ---------------------------------------------------------------
    // AUTO-ASSIGN all PENDING requests
    // ---------------------------------------------------------------
    @Transactional
    public List<MessageResponse> autoAssignAll() {
        List<DispatchOperation> pending = dispatchRepo.findPendingOrdered();
        List<MessageResponse> results = new ArrayList<>();

        for (DispatchOperation d : pending) {
            try {
                doAssign(d);
                results.add(new MessageResponse(
                        "Dispatch #" + d.getId() + " assigned successfully", true));
            } catch (Exception e) {
                results.add(new MessageResponse(
                        "Dispatch #" + d.getId() + ": " + e.getMessage(), false));
            }
        }
        return results;
    }

    // ---------------------------------------------------------------
    // Core assignment logic
    // ---------------------------------------------------------------
    private DispatchResponse doAssign(DispatchOperation dispatch) {
        // 1. Select best truck using priority algorithm
        Truck truck = priorityService.selectBestTruck(
                        dispatch.getLoadWeight(),
                        dispatch.getIsEmergency())
                .orElseThrow(() -> new NoTruckAvailableException("No suitable truck available"));

        // 2. Select an idle gate that supports this load type
        List<WarehouseGate> idleGates = gateRepo.findIdleGatesSupportingLoadType(
                dispatch.getLoadType().getId());
        if (idleGates.isEmpty()) {
            throw new NoGateAvailableException(
                    "No gate available for load type: " + dispatch.getLoadType().getName());
        }
        WarehouseGate gate = idleGates.get(0);

        // 3. Check compatibility: does the truck need cleaning?
        boolean needsCleaning = false;
        CompatibilityRule rule = null;

        if (truck.getPreviousLoadType() != null
                && !truck.getPreviousLoadType().getId().equals(dispatch.getLoadType().getId())) {
            Optional<CompatibilityRule> ruleOpt = ruleRepo.findByFromAndTo(
                    truck.getPreviousLoadType().getId(),
                    dispatch.getLoadType().getId());
            if (ruleOpt.isPresent() && ruleOpt.get().getCleaningRequired()) {
                needsCleaning = true;
                rule = ruleOpt.get();
            }
        }

        // 4. Calculate expected minutes: max(10, ceil(loadWeight * 2))
        int expectedMinutes = Math.max(10, (int) Math.ceil(dispatch.getLoadWeight() * 2));

        // 5. Free the truck's parking slot
        slotRepo.findByTruckId(truck.getId()).ifPresent(slot -> {
            slot.setTruck(null);
            slot.setIsOccupied(false);
            slotRepo.save(slot);
        });

        // 6. Reserve the gate
        gate.setCurrentTruck(truck);
        gate.setGateStatus(dispatch.getOperationType() == OperationType.LOADING
                ? GateStatus.LOADING : GateStatus.UNLOADING);
        gateRepo.save(gate);

        // 7. Update dispatch
        dispatch.setTruck(truck);
        dispatch.setGate(gate);
        dispatch.setExpectedMinutes(expectedMinutes);

        if (needsCleaning) {
            // Truck needs cleaning first
            truck.setCurrentStatus(TruckStatus.IN_CLEANING);
            dispatch.setStatus(DispatchStatus.IN_CLEANING);

            // Create a cleaning log
            CleaningLog cl = CleaningLog.builder()
                    .truck(truck)
                    .dispatch(dispatch)
                    .startTime(LocalDateTime.now())
                    .cost(rule.getCleaningCost())
                    .status("IN_PROGRESS")
                    .build();
            cleaningRepo.save(cl);

            log.info("Truck {} sent for cleaning ({} min, {} INR)",
                    truck.getLicensePlate(), rule.getCleaningMinutes(), rule.getCleaningCost());
        } else {
            // Go straight to the gate
            truck.setCurrentStatus(TruckStatus.AT_GATE);
            dispatch.setStatus(DispatchStatus.IN_PROGRESS);
            dispatch.setStartedAt(LocalDateTime.now());
        }

        truckRepo.save(truck);
        dispatch = dispatchRepo.save(dispatch);
        return toResponse(dispatch);
    }

    // ---------------------------------------------------------------
    // COMPLETE a dispatch
    // ---------------------------------------------------------------
    @Transactional
    public DispatchResponse complete(Long dispatchId) {
        DispatchOperation dispatch = dispatchRepo.findById(dispatchId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found: " + dispatchId));

        if (dispatch.getStatus() != DispatchStatus.IN_PROGRESS) {
            throw new IllegalStateException("Dispatch #" + dispatchId + " is not IN_PROGRESS");
        }

        dispatch.setStatus(DispatchStatus.COMPLETED);
        dispatch.setCompletedAt(LocalDateTime.now());

        // Free the gate
        if (dispatch.getGate() != null) {
            WarehouseGate gate = dispatch.getGate();
            gate.setGateStatus(GateStatus.IDLE);
            gate.setCurrentTruck(null);
            gateRepo.save(gate);
        }

        // Update truck: DISPATCHED, set previous_load_type to the load just handled
        if (dispatch.getTruck() != null) {
            Truck truck = dispatch.getTruck();
            truck.setCurrentStatus(TruckStatus.DISPATCHED);
            truck.setPreviousLoadType(dispatch.getLoadType());
            truckRepo.save(truck);
        }

        dispatch = dispatchRepo.save(dispatch);
        return toResponse(dispatch);
    }

    // ---------------------------------------------------------------
    // CANCEL a dispatch
    // ---------------------------------------------------------------
    @Transactional
    public DispatchResponse cancel(Long dispatchId) {
        DispatchOperation dispatch = dispatchRepo.findById(dispatchId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found: " + dispatchId));

        if (dispatch.getStatus() == DispatchStatus.COMPLETED ||
            dispatch.getStatus() == DispatchStatus.CANCELLED) {
            throw new IllegalStateException("Dispatch #" + dispatchId + " is already " + dispatch.getStatus());
        }

        dispatch.setStatus(DispatchStatus.CANCELLED);
        dispatch.setCompletedAt(LocalDateTime.now());

        // Free the gate if assigned
        if (dispatch.getGate() != null) {
            WarehouseGate gate = dispatch.getGate();
            gate.setGateStatus(GateStatus.IDLE);
            gate.setCurrentTruck(null);
            gateRepo.save(gate);
        }

        // Return truck to IN_PARKING if it was still in the system
        if (dispatch.getTruck() != null) {
            Truck truck = dispatch.getTruck();
            if (truck.getCurrentStatus() != TruckStatus.DISPATCHED) {
                truck.setCurrentStatus(TruckStatus.IN_PARKING);
                truckRepo.save(truck);

                // Re-assign a parking slot
                List<ParkingSlot> freeSlots = slotRepo.findFreeSlotsSorted();
                if (!freeSlots.isEmpty()) {
                    ParkingSlot slot = freeSlots.get(0);
                    slot.setTruck(truck);
                    slot.setIsOccupied(true);
                    slotRepo.save(slot);
                }
            }
        }

        dispatch = dispatchRepo.save(dispatch);
        return toResponse(dispatch);
    }

    // ---------------------------------------------------------------
    // QUERY methods
    // ---------------------------------------------------------------

    public List<DispatchResponse> findAll() {
        return dispatchRepo.findAllWithDetails().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<DispatchResponse> findPending() {
        return dispatchRepo.findPendingOrdered().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<DispatchResponse> findActive() {
        return dispatchRepo.findActive().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DispatchResponse findById(Long id) {
        DispatchOperation d = dispatchRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch not found: " + id));
        return toResponse(d);
    }

    // ---------------------------------------------------------------
    // PROGRESS CALCULATION
    // ---------------------------------------------------------------

    /**
     * Calculate progress percentage for an in-progress dispatch.
     * progress = min(100, (elapsed_seconds * speedFactor / 60) / expected_minutes * 100)
     */
    public double calculateProgress(DispatchOperation d) {
        if (d.getStatus() == DispatchStatus.COMPLETED) return 100.0;
        if (d.getStartedAt() == null || d.getExpectedMinutes() == null) return 0.0;

        long elapsedSeconds = Duration.between(d.getStartedAt(), LocalDateTime.now()).getSeconds();
        double elapsedMinutes = (elapsedSeconds * speedFactor) / 60.0;
        double progress = (elapsedMinutes / d.getExpectedMinutes()) * 100.0;
        return Math.min(100.0, Math.round(progress * 10.0) / 10.0);
    }

    // ---------------------------------------------------------------
    // DTO CONVERSION
    // ---------------------------------------------------------------

    public DispatchResponse toResponse(DispatchOperation d) {
        return DispatchResponse.builder()
                .id(d.getId())
                .truckId(d.getTruck() != null ? d.getTruck().getId() : null)
                .truckPlate(d.getTruck() != null ? d.getTruck().getLicensePlate() : null)
                .driverName(d.getTruck() != null ? d.getTruck().getDriverName() : null)
                .gateId(d.getGate() != null ? d.getGate().getId() : null)
                .gateNumber(d.getGate() != null ? d.getGate().getGateNumber() : null)
                .loadTypeName(d.getLoadType() != null ? d.getLoadType().getName() : null)
                .loadTypeId(d.getLoadType() != null ? d.getLoadType().getId() : null)
                .operationType(d.getOperationType().name())
                .loadWeight(d.getLoadWeight())
                .destinationDistance(d.getDestinationDistance())
                .isEmergency(d.getIsEmergency())
                .status(d.getStatus().name())
                .requestedAt(d.getRequestedAt())
                .startedAt(d.getStartedAt())
                .expectedMinutes(d.getExpectedMinutes())
                .completedAt(d.getCompletedAt())
                .progressPercent(calculateProgress(d))
                .build();
    }
}
