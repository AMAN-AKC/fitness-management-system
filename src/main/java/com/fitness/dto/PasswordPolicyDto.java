package com.fitness.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PasswordPolicyDto {
    private Long policyId;
    private Integer minPasswordLength;
    private Boolean requireUppercase;
    private Boolean requireNumber;
    private Boolean requireSpecialChar;
    private Integer sessionTimeoutMin;
    private Integer maxFailedAttempts;
    private Integer lockoutDurationMin;
    private String lastUpdatedBy;
    private LocalDateTime updatedAt;
}
