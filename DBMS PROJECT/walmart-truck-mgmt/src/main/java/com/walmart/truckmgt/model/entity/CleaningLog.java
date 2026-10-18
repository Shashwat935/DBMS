package com.walmart.truckmgt.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CleaningLog – records every truck cleaning event.
 * IN_PROGRESS until end_time is set, then COMPLETED.
 */
@Entity
@Table(name = "cleaning_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CleaningLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The truck being cleaned */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;

    /** The dispatch operation that triggered this cleaning */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dispatch_id")
    private DispatchOperation dispatch;

    /** When cleaning started */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /** When cleaning finished (null while in progress) */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /** Cost of this cleaning in INR */
    @Column(name = "cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    /** IN_PROGRESS or COMPLETED */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "IN_PROGRESS";

    /** Expected cleaning duration in minutes (from CompatibilityRule) */
    @Transient
    private Integer cleaningMinutes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
