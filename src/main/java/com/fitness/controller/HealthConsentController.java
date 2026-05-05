package com.fitness.controller;

import com.fitness.dto.HealthConsentDTO;
import com.fitness.service.HealthConsentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/consents")
@RequiredArgsConstructor
@Tag(name = "Health Consent", description = "PAR-Q and waiver management (US09)")
public class HealthConsentController {

	private final HealthConsentService consentService;

	@PostMapping
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','ADMIN')")
	public ResponseEntity<HealthConsentDTO> submitConsent(@Valid @RequestBody HealthConsentDTO dto,
			HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(consentService.submitConsent(dto, request.getRemoteAddr()));
	}

	@GetMapping("/member/{memberId}")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<List<HealthConsentDTO>> getConsentsByMember(@PathVariable Long memberId) {
		return ResponseEntity.ok(consentService.getConsentsByMember(memberId));
	}

	@GetMapping("/member/{memberId}/active")
	@PreAuthorize("hasAnyRole('FRONT_DESK','TRAINER','MANAGER','ADMIN')")
	public ResponseEntity<Boolean> hasActiveConsent(@PathVariable Long memberId) {
		return ResponseEntity.ok(consentService.hasActiveConsent(memberId));
	}
}
