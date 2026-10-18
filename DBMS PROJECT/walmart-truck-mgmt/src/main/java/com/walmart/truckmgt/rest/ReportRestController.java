package com.walmart.truckmgt.rest;

import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.model.entity.LoadType;
import com.walmart.truckmgt.service.DashboardService;
import com.walmart.truckmgt.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Reports, Dashboard, and Master Data.
 * Consolidates aggregate queries, compatibility checks, and load types.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportRestController {

    private final ReportService reportService;
    private final DashboardService dashboardService;

    // ---- Dashboard ----

    /** GET /api/dashboard/summary – dashboard summary counts */
    @GetMapping("/dashboard/summary")
    public DashboardSummary getDashboardSummary() {
        return dashboardService.getSummary();
    }

    // ---- Reports ----

    /** GET /api/reports/weight-by-load-type – total weight dispatched per load type */
    @GetMapping("/reports/weight-by-load-type")
    public List<WeightByLoadTypeProjection> getWeightByLoadType() {
        return reportService.getWeightByLoadType();
    }

    /** GET /api/reports/gate-performance – dispatches per gate + avg duration */
    @GetMapping("/reports/gate-performance")
    public List<GatePerformanceProjection> getGatePerformance() {
        return reportService.getGatePerformance();
    }

    /** GET /api/reports/size-class-counts – trucks per size class in parking */
    @GetMapping("/reports/size-class-counts")
    public List<SizeClassCountProjection> getSizeClassCounts() {
        return reportService.getSizeClassCounts();
    }

    /** GET /api/reports/daily-summary – daily dispatch totals */
    @GetMapping("/reports/daily-summary")
    public List<DailySummaryProjection> getDailySummary() {
        return reportService.getDailySummary();
    }

    /** GET /api/reports/active-trucks – UNION of trucks at gate + in cleaning */
    @GetMapping("/reports/active-trucks")
    public List<TruckLedgerProjection> getActiveTrucks() {
        return reportService.getActiveTrucks();
    }

    // ---- Master Data: Load Types ----

    /** GET /api/load-types – all load types */
    @GetMapping("/load-types")
    public List<LoadType> getLoadTypes() {
        return reportService.getAllLoadTypes();
    }

    // ---- Compatibility ----

    /** GET /api/compatibility – all compatibility rules */
    @GetMapping("/compatibility")
    public List<CompatibilityResponse> getCompatibilityRules() {
        return reportService.getAllRules();
    }

    /** GET /api/compatibility/check?from=&to= – check compatibility between two load types */
    @GetMapping("/compatibility/check")
    public CompatibilityResponse checkCompatibility(
            @RequestParam Long from, @RequestParam Long to) {
        return reportService.checkCompatibility(from, to);
    }
}
