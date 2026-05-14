package com.fitness.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "FEATURE_FLAG")
@Data
public class FeatureFlag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flagId;

    @Column(nullable = false, unique = true)
    private String flagName;

    @Column(nullable = false)
    private boolean isEnabled;

    private String lastModifiedBy;

    @Column(insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
