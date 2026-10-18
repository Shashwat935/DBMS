package com.walmart.truckmgt.rest;

import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.service.CleaningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Cleaning operations.
 * Handles completing cleaning logs and querying cleaning data.
 */
@RestController
@RequestMapping("/api/cleaning")
@RequiredArgsConstructor
public class CleaningRestController {

    private final CleaningService cleaningService;

    /** GET /api/cleaning/active – currently in-progress cleaning operations */
    @GetMapping("/active")
    public List<CleaningLogResponse> getActive() {
        return cleaningService.findActive();
    }

    /** GET /api/cleaning/logs – all cleaning logs (history) */
    @GetMapping("/logs")
    public List<CleaningLogResponse> getLogs() {
        return cleaningService.findAll();
    }

    /** POST /api/cleaning/{id}/complete – complete a cleaning operation */
    @PostMapping("/{id}/complete")
    public CleaningLogResponse complete(@PathVariable Long id) {
        return cleaningService.completeCleaning(id);
    }

    /** GET /api/cleaning/cost-by-truck?min= – cleaning cost per truck above minimum */
    @GetMapping("/cost-by-truck")
    public List<CleaningCostProjection> getCostByTruck(
            @RequestParam(defaultValue = "0") double min) {
        return cleaningService.getCostByTruck(min);
    }
}
