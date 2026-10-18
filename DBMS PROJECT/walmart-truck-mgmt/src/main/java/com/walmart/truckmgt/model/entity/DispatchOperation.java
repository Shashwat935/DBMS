package com.walmart.truckmgt.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DispatchOperation – a request to load or unload cargo via a warehouse gate.
 * Lifecycle: PENDING → ASSIGNED → (IN_CLEANING →) IN_PROGRESS → COMPLETED/CANCELLED
 */
@Entity
@Table(name = "dispatch_operation", indexes = {
    @Index(name = "idx_dispatch_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DispatchOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The truck assigned to this operation (null until assigned) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "truck_id")
    private Truck truck;

    /** The gate assigned to this operation (null until assigned) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gate_id")
    private WarehouseGate gate;

    /** What type of cargo this operation handles */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "load_type_id", nullable = false)
    private LoadType loadType;

    /** LOADING or UNLOADING */
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 20)
    private OperationType operationType;

    /** Weight of the cargo in tons */
    @Column(name = "load_weight", nullable = false)
    private Double loadWeight;

    /** Distance to destination in km */
    @Column(name = "destination_distance", nullable = false)
    private Double destinationDistance = 0.0;

    /** Whether this is an emergency request (gets priority) */
    @Column(name = "is_emergency", nullable = false)
    private Boolean isEmergency = false;

    /** Current status in the lifecycle */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DispatchStatus status = DispatchStatus.PENDING;

    /** When the dispatch was requested */
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    /** When loading/unloading actually started at the gate */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /** Expected duration of the operation in minutes */
    @Column(name = "expected_minutes")
    private Integer expectedMinutes;

    /** When the operation was completed */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
