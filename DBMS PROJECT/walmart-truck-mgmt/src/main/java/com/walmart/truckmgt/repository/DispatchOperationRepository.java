package com.walmart.truckmgt.repository;

import com.walmart.truckmgt.model.dto.*;
import com.walmart.truckmgt.model.entity.DispatchOperation;
import com.walmart.truckmgt.model.entity.DispatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for DispatchOperation with JOIN and aggregate queries.
 */
@Repository
public interface DispatchOperationRepository extends JpaRepository<DispatchOperation, Long> {

    /** Find dispatches by status */
    List<DispatchOperation> findByStatus(DispatchStatus status);

    /** Find dispatches by status, ordered: emergency first, then by requested_at */
    @Query("SELECT d FROM DispatchOperation d WHERE d.status = 'PENDING' " +
           "ORDER BY d.isEmergency DESC, d.requestedAt ASC")
    List<DispatchOperation> findPendingOrdered();

    /** Count dispatches by status */
    long countByStatus(DispatchStatus status);

    /** Active operations (ASSIGNED, IN_CLEANING, IN_PROGRESS) */
    @Query("SELECT d FROM DispatchOperation d WHERE d.status IN " +
           "('ASSIGNED','IN_CLEANING','IN_PROGRESS') " +
           "ORDER BY d.requestedAt ASC")
    List<DispatchOperation> findActive();

    /**
     * JOIN QUERY 2: Dispatch history with all details.
     * Fetches all linked entities eagerly.
     */
    @Query("SELECT d FROM DispatchOperation d " +
           "LEFT JOIN FETCH d.truck " +
           "LEFT JOIN FETCH d.gate " +
           "JOIN FETCH d.loadType " +
           "ORDER BY d.requestedAt DESC")
    List<DispatchOperation> findAllWithDetails();

    /**
     * AGGREGATE 9: Total weight dispatched per load type (completed only).
     */
    @Query(value =
        "SELECT lt.name AS loadType, COUNT(d.id) AS dispatchCount, " +
        "       SUM(d.load_weight) AS totalWeight " +
        "FROM dispatch_operation d " +
        "JOIN load_type lt ON d.load_type_id = lt.id " +
        "WHERE d.status = 'COMPLETED' " +
        "GROUP BY lt.id, lt.name ORDER BY totalWeight DESC",
        nativeQuery = true)
    List<WeightByLoadTypeProjection> findWeightByLoadType();

    /**
     * AGGREGATE 12: Daily dispatch totals.
     */
    @Query(value =
        "SELECT DATE(d.completed_at) AS dispatchDate, COUNT(d.id) AS dispatchCount, " +
        "       SUM(d.load_weight) AS totalWeight " +
        "FROM dispatch_operation d " +
        "WHERE d.status = 'COMPLETED' AND d.completed_at IS NOT NULL " +
        "GROUP BY DATE(d.completed_at) ORDER BY dispatchDate DESC",
        nativeQuery = true)
    List<DailySummaryProjection> findDailySummary();
}
