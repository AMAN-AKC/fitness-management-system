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
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.Operation;

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

	@GetMapping("/expiring")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	public ResponseEntity<List<MembershipDTO>> getExpiringMemberships(
			@RequestParam(defaultValue = "7") int daysAhead) {
		return ResponseEntity.ok(membershipService.getExpiringMemberships(daysAhead));
	}
}