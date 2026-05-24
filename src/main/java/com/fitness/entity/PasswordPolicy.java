package com.fitness.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "PASSWORD_POLICY")
@Data
public class PasswordPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policyId;

    @Column(nullable = false)
    private Integer minPasswordLength = 8;

    @Column(nullable = false)
    private Boolean requireUppercase = true;

    @Column(nullable = false)
    private Boolean requireNumber = true;

    @Column(nullable = false)
    private Boolean requireSpecialChar = true;

    @Column(nullable = false)
    private Integer sessionTimeoutMin = 60;

    @Column(nullable = false)
    private Integer maxFailedAttempts = 5;

    @Column(nullable = false)
    private Integer lockoutDurationMin = 30;

    @Column(nullable = false, length = 80)
    private String lastUpdatedBy = "SYSTEM";

    @Column(insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
