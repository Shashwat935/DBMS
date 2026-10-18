package com.walmart.truckmgt.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CompatibilityRule – defines whether cleaning is required when a truck
 * switches from one load type to another, and the cost/time if so.
 * UNIQUE constraint on (from_load_type, to_load_type).
 */
@Entity
@Table(name = "compatibility_rule",
       uniqueConstraints = @UniqueConstraint(columnNames = {"from_load_type_id", "to_load_type_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompatibilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The load type the truck previously carried */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_load_type_id", nullable = false)
    private LoadType fromLoadType;

    /** The load type the truck will carry next */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_load_type_id", nullable = false)
    private LoadType toLoadType;

    /** Whether the truck needs cleaning for this transition */
    @Column(name = "cleaning_required", nullable = false)
    private Boolean cleaningRequired = false;

    /** Cost of cleaning in INR */
    @Column(name = "cleaning_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal cleaningCost = BigDecimal.ZERO;

    /** Time required for cleaning in minutes */
    @Column(name = "cleaning_minutes", nullable = false)
    private Integer cleaningMinutes = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
