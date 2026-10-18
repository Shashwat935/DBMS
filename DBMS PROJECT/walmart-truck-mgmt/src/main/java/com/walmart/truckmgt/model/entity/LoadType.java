package com.walmart.truckmgt.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * LoadType – master list of cargo categories (COAL, IRON, FOOD, etc.).
 * Each load type belongs to a category: FOOD_GRADE, INDUSTRIAL, or GENERAL.
 */
@Entity
@Table(name = "load_type")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoadType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique name like COAL, IRON, FOOD, CHEMICAL, TEXTILE, ELECTRONICS */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /** Category grouping: FOOD_GRADE, INDUSTRIAL, GENERAL */
    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
