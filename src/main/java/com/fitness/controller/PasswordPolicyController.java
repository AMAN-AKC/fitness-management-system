package com.fitness.controller;

import com.fitness.dto.PasswordPolicyDto;
import com.fitness.service.PasswordPolicyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/config/password-policy")
@RequiredArgsConstructor
@Tag(name = "Password Policy", description = "System-wide password & session policy (admin-managed)")
public class PasswordPolicyController {

    private final PasswordPolicyService policyService;

    /**
     * GET  — readable by any authenticated user so the frontend
     *         can enforce the policy at registration and reset-password.
     */
    @GetMapping
    public ResponseEntity<PasswordPolicyDto> getPolicy() {
        return ResponseEntity.ok(policyService.getPolicy());
    }

    /**
     * PUT  — restricted to ADMIN only.
     *         The authenticated username is automatically recorded in lastUpdatedBy.
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PasswordPolicyDto> updatePolicy(
            @RequestBody PasswordPolicyDto dto,
            Authentication auth) {
        String updatedBy = auth != null ? auth.getName() : "ADMIN";
        return ResponseEntity.ok(policyService.updatePolicy(dto, updatedBy));
    }
}
