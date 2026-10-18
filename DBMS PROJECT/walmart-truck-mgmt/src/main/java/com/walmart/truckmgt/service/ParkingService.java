package com.walmart.truckmgt.service;

import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.model.entity.*;
import com.walmart.truckmgt.repository.ParkingSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ParkingService – handles parking slot queries and truck release.
 */
@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingSlotRepository slotRepo;

    /** Get all 40 slots with truck info (JOIN query) */
    public List<ParkingSlotResponse> getAllSlots() {
        return slotRepo.findAllWithTrucks().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Get parking area stats */
    public ParkingStatsResponse getStats() {
        long total    = slotRepo.count();
        long occupied = slotRepo.countByIsOccupied(true);
        return ParkingStatsResponse.builder()
                .totalSlots(total)
                .occupiedSlots(occupied)
                .freeSlots(total - occupied)
                .build();
    }

    /** Release a truck from its parking slot */
    @Transactional
    public void releaseSlot(Long truckId) {
        slotRepo.findByTruckId(truckId).ifPresent(slot -> {
            slot.setTruck(null);
            slot.setIsOccupied(false);
            slotRepo.save(slot);
        });
    }

    private ParkingSlotResponse toResponse(ParkingSlot ps) {
        Truck t = ps.getTruck();
        return ParkingSlotResponse.builder()
                .id(ps.getId())
                .slotNumber(ps.getSlotNumber())
                .rowLabel(ps.getRowLabel())
                .colNumber(ps.getColNumber())
                .isOccupied(ps.getIsOccupied())
                .truckId(t != null ? t.getId() : null)
                .licensePlate(t != null ? t.getLicensePlate() : null)
                .driverName(t != null ? t.getDriverName() : null)
                .sizeClass(t != null ? t.getSizeClass().name() : null)
                .build();
    }
}
