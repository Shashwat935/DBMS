package com.walmart.truckmgt.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ParkingSlot – one of 40 parking spaces in a 4x10 grid (A1-D10).
 * The ParkingSlot owns the FK to Truck (owning side of 1-1 relationship).
 */
@Entity
@Table(name = "parking_slot", indexes = {
    @Index(name = "idx_parking_occupied", columnList = "is_occupied")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique slot label like A1, B5, D10 */
    @Column(name = "slot_number", nullable = false, unique = true, length = 10)
    private String slotNumber;

    /** Row letter: A, B, C, or D */
    @Column(name = "row_label", nullable = false, length = 1)
    private String rowLabel;

    /** Column number: 1 through 10 */
    @Column(name = "col_number", nullable = false)
    private Integer colNumber;

    /** Whether a truck is currently parked here */
    @Column(name = "is_occupied", nullable = false)
    private Boolean isOccupied = false;

    /** The truck parked in this slot (null if empty) */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "truck_id", unique = true)
    private Truck truck;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
