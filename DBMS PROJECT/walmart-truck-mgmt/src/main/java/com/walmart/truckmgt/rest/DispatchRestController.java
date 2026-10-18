package com.walmart.truckmgt.rest;

import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.service.DispatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Dispatch operations.
 * Handles request creation, assignment, completion, and queries.
 */
@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
public class DispatchRestController {

    private final DispatchService dispatchService;

    /** POST /api/dispatch/request – create a new dispatch request (PENDING) */
    @PostMapping("/request")
    public ResponseEntity<DispatchResponse> createRequest(@Valid @RequestBody DispatchRequest req) {
        return ResponseEntity.status(201).body(dispatchService.createRequest(req));
    }

    /** GET /api/dispatch – all dispatches */
    @GetMapping
    public List<DispatchResponse> getAll() {
        return dispatchService.findAll();
    }

    /** GET /api/dispatch/pending – pending dispatches ordered by priority */
    @GetMapping("/pending")
    public List<DispatchResponse> getPending() {
        return dispatchService.findPending();
    }

    /** GET /api/dispatch/active – active dispatches (assigned, in_cleaning, in_progress) */
    @GetMapping("/active")
    public List<DispatchResponse> getActive() {
        return dispatchService.findActive();
    }

    /** GET /api/dispatch/history – all dispatches with full details */
    @GetMapping("/history")
    public List<DispatchResponse> getHistory() {
        return dispatchService.findAll();
    }

    /** POST /api/dispatch/{id}/assign – assign a single PENDING request */
    @PostMapping("/{id}/assign")
    public DispatchResponse assign(@PathVariable Long id) {
        return dispatchService.assignSingle(id);
    }

    /** POST /api/dispatch/auto-assign – auto-assign all PENDING requests */
    @PostMapping("/auto-assign")
    public List<MessageResponse> autoAssign() {
        return dispatchService.autoAssignAll();
    }

    /** POST /api/dispatch/{id}/complete – complete an IN_PROGRESS dispatch */
    @PostMapping("/{id}/complete")
    public DispatchResponse complete(@PathVariable Long id) {
        return dispatchService.complete(id);
    }

    /** POST /api/dispatch/{id}/cancel – cancel a dispatch */
    @PostMapping("/{id}/cancel")
    public DispatchResponse cancel(@PathVariable Long id) {
        return dispatchService.cancel(id);
    }
}
