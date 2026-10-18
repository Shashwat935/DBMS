package com.walmart.truckmgt.rest;

import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.service.GateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Warehouse Gate operations.
 * All endpoints under /api/gates return JSON via DTOs.
 */
@RestController
@RequestMapping("/api/gates")
@RequiredArgsConstructor
public class GateRestController {

    private final GateService gateService;

    /** GET /api/gates – list all gates */
    @GetMapping
    public List<GateResponse> getAll() {
        return gateService.findAll();
    }

    /** GET /api/gates/overview – rich gate overview with truck/dispatch info */
    @GetMapping("/overview")
    public List<GateOverviewResponse> getOverview() {
        return gateService.getOverview();
    }

    /** GET /api/gates/{id} – single gate by ID */
    @GetMapping("/{id}")
    public GateResponse getById(@PathVariable Long id) {
        return gateService.findById(id);
    }

    /** POST /api/gates – create a new gate */
    @PostMapping
    public GateResponse create(@RequestBody GateResponse req) {
        return gateService.create(req);
    }

    /** PUT /api/gates/{id}/load-types – update a gate's supported load types */
    @PutMapping("/{id}/load-types")
    public GateResponse updateLoadTypes(@PathVariable Long id, @RequestBody List<Long> loadTypeIds) {
        return gateService.updateLoadTypes(id, loadTypeIds);
    }
}
