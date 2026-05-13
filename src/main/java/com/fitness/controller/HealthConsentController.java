package com.fitness.controller;

import com.fitness.dto.HealthConsentDTO;
import com.fitness.dto.MemberDTO;
import com.fitness.entity.Member;
import com.fitness.service.HealthConsentService;
import com.fitness.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/consents")
@RequiredArgsConstructor
@Tag(name = "Health Consent", description = "PAR-Q and waiver management (US09)")
public class HealthConsentController {

	private final HealthConsentService consentService;
	private final MemberService memberService;

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

	@GetMapping("/member/{memberId}/status")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','TRAINER','MANAGER','ADMIN')")
	public ResponseEntity<Map<String, Object>> getConsentStatus(@PathVariable Long memberId) {
		return ResponseEntity.ok(consentService.getConsentStatus(memberId));
	}

	@GetMapping("/me/member")
	@PreAuthorize("hasRole('MEMBER')")
	public ResponseEntity<MemberDTO> getCurrentMember() {
		Member member = consentService.getCurrentMember();
		return ResponseEntity.ok(memberService.getMemberById(member.getMemberId()));
	}

	@PatchMapping("/{consentId}/notes")
	@PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<HealthConsentDTO> addAdministrativeNote(@PathVariable Long consentId,
			@RequestBody Map<String, String> body) {
		return ResponseEntity.ok(consentService.addAdministrativeNote(consentId, body.get("note")));
	}

	@GetMapping("/member/{memberId}/download")
	@PreAuthorize("hasAnyRole('MEMBER','ADMIN')")
	public ResponseEntity<byte[]> downloadConsentHistory(@PathVariable Long memberId) {
		byte[] pdf = consentService.downloadConsentHistoryPdf(memberId);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"consent-history.pdf\"")
				.body(pdf);
	}

	@GetMapping("/stats/anonymized")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	public ResponseEntity<List<Map<String, Object>>> anonymizedStats() {
		return ResponseEntity.ok(consentService.exportAnonymizedHealthStats());
	}

	@GetMapping("/policy")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<Map<String, Object>> getPolicy() {
		return ResponseEntity.ok(consentService.getRetentionPolicy());
	}

	@DeleteMapping("/retention/expired")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> purgeByRetentionPolicy() {
		return ResponseEntity.ok(Map.of("deletedCount", consentService.deleteExpiredByRetentionPolicy()));
	}
}
