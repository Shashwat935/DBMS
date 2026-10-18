package com.walmart.truckmgt.repository;

import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.model.entity.Truck;
import com.walmart.truckmgt.model.entity.TruckStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Truck entity with custom JPQL and native queries.
 */
@Repository
public interface TruckRepository extends JpaRepository<Truck, Long> {

    /** Find a truck by its unique license plate */
    Optional<Truck> findByLicensePlate(String licensePlate);

    /** Find all trucks with a given status */
    List<Truck> findByCurrentStatus(TruckStatus status);

    /** Count trucks by status */
    long countByCurrentStatus(TruckStatus status);

    /**
     * UNION QUERY 6: Combined ledger – trucks IN_PARKING + trucks IN_CLEANING.
     * Uses native SQL because JPQL does not support UNION.
     */
    @Query(value =
        "SELECT t.id AS truckId, t.license_plate AS licensePlate, t.current_status AS currentStatus, " +
        "       t.location_distance AS locationDistance, t.arrival_timestamp AS arrivalTimestamp, " +
        "       'PARKING' AS source " +
        "FROM truck t WHERE t.current_status = 'IN_PARKING' " +
        "UNION " +
        "SELECT t.id AS truckId, t.license_plate AS licensePlate, t.current_status AS currentStatus, " +
        "       t.location_distance AS locationDistance, t.arrival_timestamp AS arrivalTimestamp, " +
        "       'CLEANING' AS source " +
        "FROM truck t WHERE t.current_status = 'IN_CLEANING' " +
        "ORDER BY source, truckId",
        nativeQuery = true)
    List<TruckLedgerProjection> findCombinedLedger();

    /**
     * UNION QUERY 7: All active trucks – at gates + in cleaning.
     */
    @Query(value =
        "SELECT t.id AS truckId, t.license_plate AS licensePlate, t.current_status AS currentStatus, " +
        "       t.location_distance AS locationDistance, t.arrival_timestamp AS arrivalTimestamp, " +
        "       'AT_GATE' AS source " +
        "FROM truck t WHERE t.current_status = 'AT_GATE' " +
        "UNION " +
        "SELECT t.id AS truckId, t.license_plate AS licensePlate, t.current_status AS currentStatus, " +
        "       t.location_distance AS locationDistance, t.arrival_timestamp AS arrivalTimestamp, " +
        "       'IN_CLEANING' AS source " +
        "FROM truck t WHERE t.current_status = 'IN_CLEANING' " +
        "ORDER BY source, truckId",
        nativeQuery = true)
    List<TruckLedgerProjection> findActiveTrucks();

    /**
     * SUBQUERY: Trucks whose capacity is above the average of all trucks.
     */
    @Query("SELECT t FROM Truck t WHERE t.maxWeightCapacity > " +
           "(SELECT AVG(t2.maxWeightCapacity) FROM Truck t2) " +
           "ORDER BY t.maxWeightCapacity DESC")
    List<Truck> findAboveAverageCapacity();

    /**
     * AGGREGATE 11: Trucks per size class currently in parking.
     */
    @Query(value =
        "SELECT t.size_class AS sizeClass, COUNT(*) AS truckCount " +
        "FROM truck t WHERE t.current_status = 'IN_PARKING' " +
        "GROUP BY t.size_class",
        nativeQuery = true)
    List<SizeClassCountProjection> countBySizeClassInParking();

    /**
     * Candidate trucks for dispatch: in parking and capacity >= required weight.
     */
    @Query("SELECT t FROM Truck t WHERE t.currentStatus = 'IN_PARKING' " +
           "AND t.maxWeightCapacity >= :weight")
    List<Truck> findCandidateTrucks(@Param("weight") double weight);
}
