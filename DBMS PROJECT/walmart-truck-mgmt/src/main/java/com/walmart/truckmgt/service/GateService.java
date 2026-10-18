package com.walmart.truckmgt.service;

import com.walmart.truckmgt.exception.ResourceNotFoundException;
import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.model.entity.*;
import com.walmart.truckmgt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * GateService – handles warehouse gate queries and updates.
 */
@Service
@RequiredArgsConstructor
public class GateService {

    private final WarehouseGateRepository gateRepo;
    private final LoadTypeRepository loadTypeRepo;

    /** Get all gates as DTOs */
    public List<GateResponse> findAll() {
        return gateRepo.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Get a single gate by ID */
    public GateResponse findById(Long id) {
        WarehouseGate g = gateRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found: " + id));
        return toResponse(g);
    }

    /** Create a new gate */
    @Transactional
    public GateResponse create(GateResponse req) {
        WarehouseGate gate = WarehouseGate.builder()
                .gateNumber(req.getGateNumber())
                .gateStatus(GateStatus.IDLE)
                .supportedLoadTypes(new java.util.HashSet<>())
                .build();
        gate = gateRepo.save(gate);
        return toResponse(gate);
    }

    /** Update a gate's supported load types */
    @Transactional
    public GateResponse updateLoadTypes(Long gateId, List<Long> loadTypeIds) {
        WarehouseGate gate = gateRepo.findById(gateId)
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found: " + gateId));
        Set<LoadType> types = loadTypeIds.stream()
                .map(id -> loadTypeRepo.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Load type not found: " + id)))
                .collect(Collectors.toSet());
        gate.setSupportedLoadTypes(types);
        gate = gateRepo.save(gate);
        return toResponse(gate);
    }

    /** Gate overview with dispatch/truck info (for the overview page) */
    public List<GateOverviewResponse> getOverview() {
        return gateRepo.findAll().stream().map(g -> {
            Truck t = g.getCurrentTruck();
            GateOverviewResponse.GateOverviewResponseBuilder b = GateOverviewResponse.builder()
                    .gateId(g.getId())
                    .gateNumber(g.getGateNumber())
                    .gateStatus(g.getGateStatus().name())
                    .truckId(t != null ? t.getId() : null)
                    .licensePlate(t != null ? t.getLicensePlate() : null)
                    .driverName(t != null ? t.getDriverName() : null)
                    .isCleaning(t != null && t.getCurrentStatus() == TruckStatus.IN_CLEANING);
            return b.build();
        }).collect(Collectors.toList());
    }

    private GateResponse toResponse(WarehouseGate g) {
        return GateResponse.builder()
                .id(g.getId())
                .gateNumber(g.getGateNumber())
                .gateStatus(g.getGateStatus().name())
                .currentTruckId(g.getCurrentTruck() != null ? g.getCurrentTruck().getId() : null)
                .currentTruckPlate(g.getCurrentTruck() != null ? g.getCurrentTruck().getLicensePlate() : null)
                .supportedLoadTypes(g.getSupportedLoadTypes().stream()
                        .map(LoadType::getName)
                        .sorted()
                        .collect(Collectors.toList()))
                .build();
    }
}
