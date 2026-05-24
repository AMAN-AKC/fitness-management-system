package com.fitness.service;

import com.fitness.dto.PasswordPolicyDto;
import com.fitness.entity.PasswordPolicy;
import com.fitness.repository.PasswordPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordPolicyService {

    private static final Long POLICY_ID = 1L;

    private final PasswordPolicyRepository repository;

    /** GET — any authenticated user can read the policy */
    public PasswordPolicyDto getPolicy() {
        PasswordPolicy entity = repository.findById(POLICY_ID)
                .orElseGet(this::createDefaultPolicy);
        return toDto(entity);
    }

    /** PUT — admin saves an updated policy */
    public PasswordPolicyDto updatePolicy(PasswordPolicyDto dto, String updatedBy) {
        PasswordPolicy entity = repository.findById(POLICY_ID)
                .orElseGet(this::createDefaultPolicy);

        entity.setMinPasswordLength(dto.getMinPasswordLength());
        entity.setRequireUppercase(dto.getRequireUppercase());
        entity.setRequireNumber(dto.getRequireNumber());
        entity.setRequireSpecialChar(dto.getRequireSpecialChar());
        entity.setSessionTimeoutMin(dto.getSessionTimeoutMin());
        entity.setMaxFailedAttempts(dto.getMaxFailedAttempts());
        entity.setLockoutDurationMin(dto.getLockoutDurationMin());
        entity.setLastUpdatedBy(updatedBy != null ? updatedBy : "ADMIN");

        log.info("Password policy updated by [{}]: minLen={}, upper={}, num={}, special={}",
                updatedBy,
                dto.getMinPasswordLength(),
                dto.getRequireUppercase(),
                dto.getRequireNumber(),
                dto.getRequireSpecialChar());

        return toDto(repository.save(entity));
    }

    /** Seed the default row if for any reason it doesn't exist yet */
    private PasswordPolicy createDefaultPolicy() {
        log.warn("PASSWORD_POLICY row missing — seeding defaults");
        PasswordPolicy p = new PasswordPolicy();
        p.setPolicyId(POLICY_ID);
        p.setMinPasswordLength(8);
        p.setRequireUppercase(true);
        p.setRequireNumber(true);
        p.setRequireSpecialChar(true);
        p.setSessionTimeoutMin(60);
        p.setMaxFailedAttempts(5);
        p.setLockoutDurationMin(30);
        p.setLastUpdatedBy("SYSTEM");
        return repository.save(p);
    }

    private PasswordPolicyDto toDto(PasswordPolicy e) {
        PasswordPolicyDto dto = new PasswordPolicyDto();
        dto.setPolicyId(e.getPolicyId());
        dto.setMinPasswordLength(e.getMinPasswordLength());
        dto.setRequireUppercase(e.getRequireUppercase());
        dto.setRequireNumber(e.getRequireNumber());
        dto.setRequireSpecialChar(e.getRequireSpecialChar());
        dto.setSessionTimeoutMin(e.getSessionTimeoutMin());
        dto.setMaxFailedAttempts(e.getMaxFailedAttempts());
        dto.setLockoutDurationMin(e.getLockoutDurationMin());
        dto.setLastUpdatedBy(e.getLastUpdatedBy());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
