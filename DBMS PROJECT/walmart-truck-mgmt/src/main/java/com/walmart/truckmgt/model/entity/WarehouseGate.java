package com.walmart.truckmgt.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * WarehouseGate – a loading/unloading gate at the warehouse.
 * Uses @Version for optimistic locking to handle concurrent access.
 * Has a many-to-many relationship with LoadType via gate_load_type table.
 */
@Entity
@Table(name = "warehouse_gate")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseGate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique gate identifier, e.g. G1, G2 */
    @Column(name = "gate_number", nullable = false, unique = true, length = 10)
    private String gateNumber;

    /** Current status: IDLE, LOADING, or UNLOADING */
    @Enumerated(EnumType.STRING)
    @Column(name = "gate_status", nullable = false, length = 20)
    private GateStatus gateStatus = GateStatus.IDLE;

    /** The truck currently at this gate (null if idle) */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_truck_id", unique = true)
    private Truck currentTruck;

    /** Optimistic locking version – prevents concurrent gate assignment conflicts */
    @Version
    @Column(name = "version")
    private Integer version;

    /** Load types that this gate can handle (many-to-many) */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "gate_load_type",
        joinColumns = @JoinColumn(name = "gate_id"),
        inverseJoinColumns = @JoinColumn(name = "load_type_id")
    )
    @Builder.Default
    private Set<LoadType> supportedLoadTypes = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
