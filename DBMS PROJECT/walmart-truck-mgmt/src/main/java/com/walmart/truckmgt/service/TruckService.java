package com.walmart.truckmgt.service;

import com.walmart.truckmgt.exception.*;
import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.model.entity.*;
import com.walmart.truckmgt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TruckService – handles truck arrival, status changes, and queries.
 */
@Service
@RequiredArgsConstructor
public class TruckService {

    private final TruckRepository truckRepo;
    private final LoadTypeRepository loadTypeRepo;
    private final ParkingSlotRepository slotRepo;

    /**
     * Register a new truck arrival and assign the first available parking slot.
     * Business rule: first free slot = lowest row_label, then lowest col_number.
     */
    @Transactional
    public TruckResponse arriveAndPark(TruckArrivalRequest req) {
        // Check for duplicate license plate
        truckRepo.findByLicensePlate(req.getLicensePlate()).ifPresent(t -> {
            throw new IllegalStateException("Truck with plate " + req.getLicensePlate() + " already exists");
        });

        // Find first free parking slot
        List<ParkingSlot> freeSlots = slotRepo.findFreeSlotsSorted();
        if (freeSlots.isEmpty()) {
            throw new ParkingFullException("Parking full");
        }
        ParkingSlot slot = freeSlots.get(0);

        // Look up previous load type if provided
        LoadType prevLoad = null;
        if (req.getPreviousLoadTypeId() != null) {
            prevLoad = loadTypeRepo.findById(req.getPreviousLoadTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Load type not found: " + req.getPreviousLoadTypeId()));
        }

        // Create and save the truck
        Truck truck = Truck.builder()
                .licensePlate(req.getLicensePlate())
                .driverName(req.getDriverName())
                .originCity(req.getOriginCity())
                .locationDistance(req.getLocationDistance())
                .maxWeightCapacity(req.getMaxWeightCapacity())
                .previousLoadType(prevLoad)
                .currentStatus(TruckStatus.IN_PARKING)
                .arrivalTimestamp(LocalDateTime.now())
                .sizeClass(SizeClass.SMALL) // computed by @PrePersist
                .build();
        truck = truckRepo.save(truck);

        // Assign the parking slot
        slot.setTruck(truck);
        slot.setIsOccupied(true);
        slotRepo.save(slot);

        return toResponse(truck, slot.getSlotNumber());
    }

    /** Get all trucks */
    public List<TruckResponse> findAll() {
        return truckRepo.findAll().stream()
                .map(t -> toResponse(t, findSlotNumber(t.getId())))
                .collect(Collectors.toList());
    }

    /** Get truck by ID */
    public TruckResponse findById(Long id) {
        Truck t = truckRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found: " + id));
        return toResponse(t, findSlotNumber(t.getId()));
    }

    /** Get trucks by status */
    public List<TruckResponse> findByStatus(String status) {
        TruckStatus ts = TruckStatus.valueOf(status.toUpperCase());
        return truckRepo.findByCurrentStatus(ts).stream()
                .map(t -> toResponse(t, findSlotNumber(t.getId())))
                .collect(Collectors.toList());
    }

    /** Update truck details */
    @Transactional
    public TruckResponse update(Long id, TruckArrivalRequest req) {
        Truck truck = truckRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found: " + id));

        truck.setLicensePlate(req.getLicensePlate());
        truck.setDriverName(req.getDriverName());
        truck.setOriginCity(req.getOriginCity());
        truck.setLocationDistance(req.getLocationDistance());
        truck.setMaxWeightCapacity(req.getMaxWeightCapacity());

        if (req.getPreviousLoadTypeId() != null) {
            truck.setPreviousLoadType(loadTypeRepo.findById(req.getPreviousLoadTypeId()).orElse(null));
        } else {
            truck.setPreviousLoadType(null);
        }

        truck = truckRepo.save(truck);
        return toResponse(truck, findSlotNumber(truck.getId()));
    }

    /** Delete a truck (also frees its parking slot) */
    @Transactional
    public void delete(Long id) {
        Truck truck = truckRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found: " + id));
        // Free parking slot if occupied
        slotRepo.findByTruckId(id).ifPresent(slot -> {
            slot.setTruck(null);
            slot.setIsOccupied(false);
            slotRepo.save(slot);
        });
        truckRepo.delete(truck);
    }

    /** Combined ledger (UNION) */
    public List<TruckLedgerProjection> getCombinedLedger() {
        return truckRepo.findCombinedLedger();
    }

    /** Active trucks (UNION) */
    public List<TruckLedgerProjection> getActiveTrucks() {
        return truckRepo.findActiveTrucks();
    }

    /** Trucks above average capacity (SUBQUERY) */
    public List<TruckResponse> getAboveAverageCapacity() {
        return truckRepo.findAboveAverageCapacity().stream()
                .map(t -> toResponse(t, findSlotNumber(t.getId())))
                .collect(Collectors.toList());
    }

    // ---- Helper methods ----

    private String findSlotNumber(Long truckId) {
        return slotRepo.findByTruckId(truckId)
                .map(ParkingSlot::getSlotNumber)
                .orElse(null);
    }

    /** Convert entity to DTO (never expose the entity directly) */
    public TruckResponse toResponse(Truck t, String slotNumber) {
        return TruckResponse.builder()
                .id(t.getId())
                .licensePlate(t.getLicensePlate())
                .driverName(t.getDriverName())
                .originCity(t.getOriginCity())
                .locationDistance(t.getLocationDistance())
                .maxWeightCapacity(t.getMaxWeightCapacity())
                .previousLoadType(t.getPreviousLoadType() != null
                        ? t.getPreviousLoadType().getName() : null)
                .previousLoadTypeId(t.getPreviousLoadType() != null
                        ? t.getPreviousLoadType().getId() : null)
                .currentStatus(t.getCurrentStatus().name())
                .arrivalTimestamp(t.getArrivalTimestamp())
                .sizeClass(t.getSizeClass().name())
                .slotNumber(slotNumber)
                .build();
    }
}
