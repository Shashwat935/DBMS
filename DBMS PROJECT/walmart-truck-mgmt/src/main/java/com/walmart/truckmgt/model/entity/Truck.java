package com.walmart.truckmgt.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Truck – each truck that arrives at the warehouse.
 * size_class is computed automatically from max_weight_capacity before saving.
 */
@Entity
@Table(name = "truck", indexes = {
    @Index(name = "idx_truck_status", columnList = "current_status"),
    @Index(name = "idx_truck_arrival", columnList = "arrival_timestamp")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique license plate, e.g. MH-12-AB-1234 */
    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(name = "driver_name", nullable = false, length = 100)
    private String driverName;

    @Column(name = "origin_city", nullable = false, length = 100)
    private String originCity;

    /** Distance from the warehouse in km */
    @Column(name = "location_distance", nullable = false)
    private Double locationDistance;

    /** Maximum weight the truck can carry, in tons */
    @Column(name = "max_weight_capacity", nullable = false)
    private Double maxWeightCapacity;

    /** The load type the truck last carried (null = clean/new truck) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "previous_load_type_id")
    private LoadType previousLoadType;

    /** Current lifecycle status of the truck */
    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 30)
    private TruckStatus currentStatus;

    /** When the truck arrived at the warehouse */
    @Column(name = "arrival_timestamp")
    private LocalDateTime arrivalTimestamp;

    /** SMALL (<10t), MEDIUM (10-25t), LARGE (>25t) – computed before save */
    @Enumerated(EnumType.STRING)
    @Column(name = "size_class", nullable = false, length = 10)
    private SizeClass sizeClass;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Automatically compute size_class from capacity before insert/update */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        computeSizeClass();
    }

    @PreUpdate
    protected void onUpdate() {
        computeSizeClass();
    }

    private void computeSizeClass() {
        if (maxWeightCapacity == null) return;
        if (maxWeightCapacity < 10) {
            this.sizeClass = SizeClass.SMALL;
        } else if (maxWeightCapacity <= 25) {
            this.sizeClass = SizeClass.MEDIUM;
        } else {
            this.sizeClass = SizeClass.LARGE;
        }
    }
}
