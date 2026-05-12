package com.fitness.controller;

import com.fitness.dto.RecurringBillingScheduleDTO;
import com.fitness.entity.Membership;
import com.fitness.service.RecurringBillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recurring-billing")
@RequiredArgsConstructor
@Tag(name = "Recurring Billing", description = "Manage recurring billing schedules (US03)")
public class RecurringBillingController {

	private final RecurringBillingService recurringBillingService;

	@PostMapping("/membership/{membershipId}/schedule")
	@PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
	@Operation(summary = "Create recurring billing schedule for a membership")
	public ResponseEntity<RecurringBillingScheduleDTO> createSchedule(@PathVariable Long membershipId) {
		Membership membership = recurringBillingService.getMembershipById(membershipId);
		return ResponseEntity.ok(recurringBillingService.createRecurringSchedule(membership));
	}

	@GetMapping("/billing-due/{date}")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Get memberships due for billing on a specific date")
	public ResponseEntity<Map<String, Object>> getMembershipsForBilling(@PathVariable String date) {
		LocalDate billingDate = LocalDate.parse(date);
		var memberships = recurringBillingService.getMembershipsForRecurringBilling(billingDate);
		return ResponseEntity.ok(Map.of(
				"status", "success",
				"billingDate", date,
				"count", memberships.size(),
				"memberships", memberships));
	}

	@PostMapping("/run-today")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Run recurring billing for today's due memberships")
	public ResponseEntity<Map<String, Object>> runRecurringBillingNow() {
		var invoices = recurringBillingService.processRecurringBilling(LocalDate.now());
		return ResponseEntity.ok(Map.of(
				"status", "success",
				"createdInvoices", invoices.size(),
				"invoices", invoices));
	}

	@PostMapping("/{membershipId}/pause")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Pause recurring billing")
	public ResponseEntity<Map<String, Object>> pauseRecurringBilling(
			@PathVariable Long membershipId,
			@RequestParam String reason) {
		recurringBillingService.pauseRecurringBilling(membershipId, reason);
		return ResponseEntity.ok(Map.of(
				"status", "success",
				"message", "Recurring billing paused"));
	}

	@PostMapping("/{membershipId}/resume")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Resume recurring billing")
	public ResponseEntity<Map<String, Object>> resumeRecurringBilling(@PathVariable Long membershipId) {
		recurringBillingService.resumeRecurringBilling(membershipId);
		return ResponseEntity.ok(Map.of(
				"status", "success",
				"message", "Recurring billing resumed"));
	}
}
