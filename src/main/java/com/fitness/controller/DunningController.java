package com.fitness.controller;

import com.fitness.entity.Membership;
import com.fitness.service.DunningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dunning")
@RequiredArgsConstructor
@Tag(name = "Dunning Management", description = "Manage failed payments and dunning status (US03)")
public class DunningController {

	private final DunningService dunningService;

	@GetMapping("/overdue-invoices")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Get all overdue invoices")
	public ResponseEntity<Map<String, Object>> getOverdueInvoices() {
		var invoices = dunningService.getOverdueInvoices();
		return ResponseEntity.ok(Map.of(
				"status", "success",
				"count", invoices.size(),
				"invoices", invoices));
	}

	@GetMapping("/overdue-by-days")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Get invoices overdue for specified days")
	public ResponseEntity<Map<String, Object>> getInvoicesOverdueByDays(
			@RequestParam(defaultValue = "3") int days) {
		var invoices = dunningService.getInvoicesOverdueByDays(days);
		return ResponseEntity.ok(Map.of(
				"status", "success",
				"daysThreshold", days,
				"count", invoices.size(),
				"invoices", invoices));
	}

	@GetMapping("/dunning-memberships")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Get all memberships in dunning status")
	public ResponseEntity<Map<String, Object>> getDunningMemberships() {
		List<Membership> memberships = dunningService.getDunningMemberships();
		return ResponseEntity.ok(Map.of(
				"status", "success",
				"count", memberships.size(),
				"memberships", memberships));
	}

	@PostMapping("/resolve/{membershipId}")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Resolve dunning status after payment")
	public ResponseEntity<Map<String, Object>> resolveDunning(@PathVariable Long membershipId) {
		dunningService.resolveDunning(membershipId);
		return ResponseEntity.ok(Map.of(
				"status", "success",
				"message", "Dunning resolved for membership " + membershipId));
	}

	@PostMapping("/suspend/{membershipId}")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Suspend membership due to extended dunning")
	public ResponseEntity<Map<String, Object>> suspendDunningMembership(
			@PathVariable Long membershipId,
			@RequestParam String reason) {
		dunningService.suspendDunningMembership(membershipId, reason);
		return ResponseEntity.ok(Map.of(
				"status", "success",
				"message", "Membership suspended due to dunning"));
	}
}
