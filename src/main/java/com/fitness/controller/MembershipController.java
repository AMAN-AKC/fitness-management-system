package com.fitness.controller;

import com.fitness.dto.MembershipDTO;
import com.fitness.service.MembershipService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
@Tag(name = "Memberships", description = "Plan purchase and renewal (US04)")
public class MembershipController {

	private final MembershipService membershipService;

	@PostMapping
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','ADMIN')")
	public ResponseEntity<MembershipDTO> createMembership(@Valid @RequestBody MembershipDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.createMembership(dto));
	}

	@GetMapping("/member/{memberId}")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<List<MembershipDTO>> getMembershipsByMember(@PathVariable Long memberId) {
		return ResponseEntity.ok(membershipService.getMembershipsByMember(memberId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<MembershipDTO> getMembershipById(@PathVariable Long id) {
		return ResponseEntity.ok(membershipService.getMembershipById(id));
	}

	@PostMapping("/upgrade")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','ADMIN')")
	public ResponseEntity<MembershipDTO> upgradeMembership(
			@RequestParam Long memberId,
			@RequestParam Long newPlanId,
			@RequestParam(required = false) java.math.BigDecimal discountAmount) {
		return ResponseEntity.ok(membershipService.upgradeMembership(memberId, newPlanId, discountAmount));
	}

	@PatchMapping("/{id}/suspend")
	@PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<MembershipDTO> suspendMembership(
			@PathVariable Long id,
			@RequestParam(required = false) Integer months,
			@RequestParam String reason) {
		return ResponseEntity.ok(membershipService.suspendMembership(id, months, reason));
	}

	@PatchMapping("/{id}/deactivate")
	@PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<MembershipDTO> deactivateMembership(
			@PathVariable Long id,
			@RequestParam String reason) {
		return ResponseEntity.ok(membershipService.deactivateMembership(id, reason));
	}

	@PatchMapping("/{id}/reactivate")
	@PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<MembershipDTO> reactivateMembership(@PathVariable Long id) {
		return ResponseEntity.ok(membershipService.reactivateMembership(id));
	}

	@GetMapping("/expiring")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	public ResponseEntity<List<MembershipDTO>> getExpiringMemberships(
			@RequestParam(defaultValue = "7") int daysAhead) {
		return ResponseEntity.ok(membershipService.getExpiringMemberships(daysAhead));
	}
}