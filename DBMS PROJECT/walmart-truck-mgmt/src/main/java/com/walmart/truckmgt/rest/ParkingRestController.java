package com.walmart.truckmgt.rest;

import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.service.ParkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Parking Slot operations.
 * All endpoints under /api/parking return JSON via DTOs.
 */
@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingRestController {

    private final ParkingService parkingService;

    /** GET /api/parking/slots – full parking grid with truck info */
    @GetMapping("/slots")
    public List<ParkingSlotResponse> getSlots() {
        return parkingService.getAllSlots();
    }

    /** GET /api/parking/stats – total/occupied/free counts */
    @GetMapping("/stats")
    public ParkingStatsResponse getStats() {
        return parkingService.getStats();
    }

    /** POST /api/parking/release/{truckId} – release a truck from its slot */
    @PostMapping("/release/{truckId}")
    public MessageResponse release(@PathVariable Long truckId) {
        parkingService.releaseSlot(truckId);
        return new MessageResponse("Truck released from parking", true);
    }
}
