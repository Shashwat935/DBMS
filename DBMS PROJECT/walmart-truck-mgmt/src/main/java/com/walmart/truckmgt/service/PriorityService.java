package com.walmart.truckmgt.service;

import com.walmart.truckmgt.model.entity.*;
import com.walmart.truckmgt.repository.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * PriorityService – implements the truck selection priority algorithm.
 *
 * For EMERGENCY requests:
 *   Sort by location_distance ASC (nearest first), then arrival_timestamp ASC.
 *
 * For NORMAL requests:
 *   Sort by size_class DESC (LARGE > MEDIUM > SMALL), then arrival_timestamp ASC (FIFO).
 *
 * Only trucks with status IN_PARKING and capacity >= request weight are candidates.
 */
@Service
@RequiredArgsConstructor
public class PriorityService {

    private final TruckRepository truckRepo;

    /**
     * Select the best truck for a dispatch request.
     * @param loadWeight     required cargo weight (tons)
     * @param isEmergency    whether this is an emergency request
     * @return the best candidate truck, or empty if none qualify
     */
    public Optional<Truck> selectBestTruck(double loadWeight, boolean isEmergency) {
        // Get all candidate trucks: IN_PARKING and capacity >= loadWeight
        List<Truck> candidates = truckRepo.findCandidateTrucks(loadWeight);

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        Comparator<Truck> comparator;

        if (isEmergency) {
            // Emergency: nearest truck first, then earliest arrival
            comparator = Comparator
                    .comparingDouble(Truck::getLocationDistance)
                    .thenComparing(Truck::getArrivalTimestamp);
        } else {
            // Normal: largest truck first, then earliest arrival (FIFO)
            comparator = Comparator
                    .comparing(Truck::getSizeClass, (a, b) -> sizeOrder(b) - sizeOrder(a))
                    .thenComparing(Truck::getArrivalTimestamp);
        }

        candidates.sort(comparator);
        return Optional.of(candidates.get(0));
    }

    /**
     * Map SizeClass to a numeric order for sorting.
     * LARGE = 3, MEDIUM = 2, SMALL = 1
     */
    private int sizeOrder(SizeClass sc) {
        return switch (sc) {
            case LARGE  -> 3;
            case MEDIUM -> 2;
            case SMALL  -> 1;
        };
    }
}
