package com.walmart.truckmgt.rest;

import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.service.TruckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Truck operations.
 * All endpoints under /api/trucks return JSON via DTOs.
 */
@RestController
@RequestMapping("/api/trucks")
@RequiredArgsConstructor
public class TruckRestController {

    private final TruckService truckService;

    /** GET /api/trucks – list all trucks */
    @GetMapping
    public List<TruckResponse> getAll() {
        return truckService.findAll();
    }

    /** GET /api/trucks/{id} – get truck by ID */
    @GetMapping("/{id}")
    public TruckResponse getById(@PathVariable Long id) {
        return truckService.findById(id);
    }

    /** POST /api/trucks/arrive – register a new truck arrival */
    @PostMapping("/arrive")
    public ResponseEntity<TruckResponse> arrive(@Valid @RequestBody TruckArrivalRequest req) {
        return ResponseEntity.status(201).body(truckService.arriveAndPark(req));
    }

    /** PUT /api/trucks/{id} – update a truck */
    @PutMapping("/{id}")
    public TruckResponse update(@PathVariable Long id, @Valid @RequestBody TruckArrivalRequest req) {
        return truckService.update(id, req);
    }

    /** DELETE /api/trucks/{id} – delete a truck */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        truckService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/trucks/status/{status} – trucks filtered by status */
    @GetMapping("/status/{status}")
    public List<TruckResponse> getByStatus(@PathVariable String status) {
        return truckService.findByStatus(status);
    }

    /** GET /api/trucks/ledger – combined UNION ledger (parking + cleaning) */
    @GetMapping("/ledger")
    public List<TruckLedgerProjection> getLedger() {
        return truckService.getCombinedLedger();
    }

    /** GET /api/trucks/above-average-capacity – subquery: trucks above avg capacity */
    @GetMapping("/above-average-capacity")
    public List<TruckResponse> getAboveAverage() {
        return truckService.getAboveAverageCapacity();
    }
}
