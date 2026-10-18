package com.walmart.truckmgt.repository;

import com.walmart.truckmgt.model.dto.GatePerformanceProjection;
import com.walmart.truckmgt.model.entity.GateStatus;
import com.walmart.truckmgt.model.entity.WarehouseGate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for WarehouseGate entity.
 */
@Repository
public interface WarehouseGateRepository extends JpaRepository<WarehouseGate, Long> {

    /** Find a gate by its unique gate number */
    Optional<WarehouseGate> findByGateNumber(String gateNumber);

    /** Count gates by status */
    long countByGateStatus(GateStatus status);

    /** Find all idle gates */
    List<WarehouseGate> findByGateStatus(GateStatus status);

    /**
     * JOIN QUERY 3: Gates that can handle a specific load type.
     * Joins gate → gate_load_type → load_type.
     */
    @Query("SELECT g FROM WarehouseGate g JOIN g.supportedLoadTypes lt " +
           "WHERE lt.id = :loadTypeId AND g.gateStatus = 'IDLE'")
    List<WarehouseGate> findIdleGatesSupportingLoadType(@Param("loadTypeId") Long loadTypeId);

    /**
     * AGGREGATE 10: Number of dispatches per gate and average duration.
     */
    @Query(value =
        "SELECT g.gate_number AS gateNumber, COUNT(d.id) AS totalDispatches, " +
        "       ROUND(AVG(TIMESTAMPDIFF(MINUTE, d.started_at, d.completed_at)),1) AS avgDurationMin " +
        "FROM dispatch_operation d " +
        "JOIN warehouse_gate g ON d.gate_id = g.id " +
        "WHERE d.status = 'COMPLETED' AND d.started_at IS NOT NULL AND d.completed_at IS NOT NULL " +
        "GROUP BY g.id, g.gate_number ORDER BY g.gate_number",
        nativeQuery = true)
    List<GatePerformanceProjection> findGatePerformance();
}
