package com.walmart.truckmgt.service;

import com.walmart.truckmgt.model.entity.*;
import com.walmart.truckmgt.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SimulationService – runs a scheduled job every 10 seconds to:
 * 1. Auto-complete IN_PROGRESS dispatch operations whose progress reached 100%.
 * 2. Auto-complete IN_PROGRESS cleaning logs whose cleaning_minutes have elapsed.
 *
 * The speed-factor property (app.simulation.speed-factor) controls simulation speed:
 *   1 = real-time, 60 = each real second counts as one simulated minute.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationService {

    private final DispatchOperationRepository dispatchRepo;
    private final CleaningLogRepository cleaningRepo;
    private final TruckRepository truckRepo;
    private final WarehouseGateRepository gateRepo;
    private final CompatibilityRuleRepository ruleRepo;

    @Value("${app.simulation.speed-factor:1}")
    private int speedFactor;

    /**
     * Scheduled job: runs every 10 seconds to auto-complete operations and cleaning.
     */
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void autoCompleteJob() {
        autoCompleteDispatches();
        autoCompleteCleaning();
    }

    /**
     * Auto-complete IN_PROGRESS dispatches whose progress has reached 100%.
     */
    private void autoCompleteDispatches() {
        List<DispatchOperation> active = dispatchRepo.findByStatus(DispatchStatus.IN_PROGRESS);
        for (DispatchOperation d : active) {
            if (d.getStartedAt() == null || d.getExpectedMinutes() == null) continue;

            double progress = calculateProgress(d);
            if (progress >= 100.0) {
                d.setStatus(DispatchStatus.COMPLETED);
                d.setCompletedAt(LocalDateTime.now());

                // Free the gate
                if (d.getGate() != null) {
                    WarehouseGate gate = d.getGate();
                    gate.setGateStatus(GateStatus.IDLE);
                    gate.setCurrentTruck(null);
                    gateRepo.save(gate);
                }

                // Mark truck as DISPATCHED and update previous load type
                if (d.getTruck() != null) {
                    Truck truck = d.getTruck();
                    truck.setCurrentStatus(TruckStatus.DISPATCHED);
                    truck.setPreviousLoadType(d.getLoadType());
                    truckRepo.save(truck);
                }

                dispatchRepo.save(d);
                log.info("Auto-completed dispatch #{}", d.getId());
            }
        }
    }

    /**
     * Auto-complete IN_PROGRESS cleaning logs whose cleaning time has elapsed.
     */
    private void autoCompleteCleaning() {
        List<CleaningLog> activeCleaning = cleaningRepo.findByStatus("IN_PROGRESS");
        for (CleaningLog cl : activeCleaning) {
            // Find the cleaning minutes from the compatibility rule
            Integer cleaningMinutes = getCleaningMinutes(cl);
            if (cleaningMinutes == null) continue;

            long elapsedSeconds = Duration.between(cl.getStartTime(), LocalDateTime.now()).getSeconds();
            double elapsedMinutes = (elapsedSeconds * speedFactor) / 60.0;

            if (elapsedMinutes >= cleaningMinutes) {
                cl.setEndTime(LocalDateTime.now());
                cl.setStatus("COMPLETED");
                cleaningRepo.save(cl);

                // Move truck from IN_CLEANING → AT_GATE
                if (cl.getTruck() != null) {
                    cl.getTruck().setCurrentStatus(TruckStatus.AT_GATE);
                    truckRepo.save(cl.getTruck());
                }

                // Move dispatch from IN_CLEANING → IN_PROGRESS
                if (cl.getDispatch() != null) {
                    cl.getDispatch().setStatus(DispatchStatus.IN_PROGRESS);
                    cl.getDispatch().setStartedAt(LocalDateTime.now());
                    dispatchRepo.save(cl.getDispatch());
                }

                log.info("Auto-completed cleaning #{} for truck {}",
                        cl.getId(), cl.getTruck() != null ? cl.getTruck().getLicensePlate() : "?");
            }
        }
    }

    /** Get cleaning minutes from the compatibility rule for a cleaning log */
    private Integer getCleaningMinutes(CleaningLog cl) {
        if (cl.getTruck() == null || cl.getDispatch() == null) return null;
        if (cl.getTruck().getPreviousLoadType() == null || cl.getDispatch().getLoadType() == null) return null;
        return ruleRepo.findByFromAndTo(
                cl.getTruck().getPreviousLoadType().getId(),
                cl.getDispatch().getLoadType().getId())
                .map(CompatibilityRule::getCleaningMinutes)
                .orElse(null);
    }

    /** Calculate progress for a dispatch operation */
    private double calculateProgress(DispatchOperation d) {
        if (d.getStartedAt() == null || d.getExpectedMinutes() == null) return 0.0;
        long elapsedSeconds = Duration.between(d.getStartedAt(), LocalDateTime.now()).getSeconds();
        double elapsedMinutes = (elapsedSeconds * speedFactor) / 60.0;
        double progress = (elapsedMinutes / d.getExpectedMinutes()) * 100.0;
        return Math.min(100.0, progress);
    }
}
