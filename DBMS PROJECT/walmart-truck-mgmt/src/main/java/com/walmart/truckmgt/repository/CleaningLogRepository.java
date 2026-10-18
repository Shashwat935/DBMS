package com.walmart.truckmgt.repository;

import com.walmart.truckmgt.model.dto.CleaningCostProjection;
import com.walmart.truckmgt.model.entity.CleaningLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for CleaningLog entity.
 */
@Repository
public interface CleaningLogRepository extends JpaRepository<CleaningLog, Long> {

    /** Find all currently in-progress cleaning operations */
    List<CleaningLog> findByStatus(String status);

    /**
     * JOIN QUERY 4: Cleaning report – logs with truck and previous load info.
     */
    @Query("SELECT cl FROM CleaningLog cl " +
           "JOIN FETCH cl.truck t " +
           "LEFT JOIN FETCH t.previousLoadType " +
           "LEFT JOIN FETCH cl.dispatch " +
           "ORDER BY cl.startTime DESC")
    List<CleaningLog> findAllWithDetails();

    /**
     * AGGREGATE 8: Total cleaning cost per truck, with HAVING clause.
     * Only returns trucks whose total cleaning cost exceeds the given minimum.
     */
    @Query(value =
        "SELECT t.license_plate AS licensePlate, t.driver_name AS driverName, " +
        "       COUNT(cl.id) AS cleaningCount, SUM(cl.cost) AS totalCost " +
        "FROM cleaning_log cl " +
        "JOIN truck t ON cl.truck_id = t.id " +
        "GROUP BY t.id, t.license_plate, t.driver_name " +
        "HAVING SUM(cl.cost) > :minCost " +
        "ORDER BY totalCost DESC",
        nativeQuery = true)
    List<CleaningCostProjection> findCostByTruckAboveMin(@Param("minCost") double minCost);

    /**
     * Total cleaning cost across all logs.
     */
    @Query("SELECT COALESCE(SUM(cl.cost), 0) FROM CleaningLog cl")
    java.math.BigDecimal totalCleaningCost();
}
