package com.walmart.truckmgt.repository;

import com.walmart.truckmgt.model.entity.ParkingSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ParkingSlot entity.
 */
@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    /**
     * Find the first free slot, ordered by row (A first) then column (1 first).
     * This implements the "first free slot" business rule.
     */
    @Query("SELECT ps FROM ParkingSlot ps WHERE ps.isOccupied = false " +
           "ORDER BY ps.rowLabel ASC, ps.colNumber ASC")
    List<ParkingSlot> findFreeSlotsSorted();

    /** Find the slot occupied by a specific truck */
    Optional<ParkingSlot> findByTruckId(Long truckId);

    /** Count occupied and free slots */
    long countByIsOccupied(boolean occupied);

    /**
     * JOIN QUERY 5: Parking grid view – all slots with parked truck info.
     * Returns all 40 slots, LEFT JOIN with truck data.
     */
    @Query("SELECT ps FROM ParkingSlot ps LEFT JOIN FETCH ps.truck " +
           "ORDER BY ps.rowLabel, ps.colNumber")
    List<ParkingSlot> findAllWithTrucks();
}
