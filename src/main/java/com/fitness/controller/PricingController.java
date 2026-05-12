package com.fitness.controller;

import com.fitness.dto.PriceBreakdownDTO;
import com.fitness.service.PriceBreakdownService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
@Tag(name = "Pricing", description = "Plan pricing and breakdown (US03)")
public class PricingController {

	private final PriceBreakdownService priceBreakdownService;

	@GetMapping("/plan/{planId}/breakdown")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	@Operation(summary = "Get price breakdown for new plan purchase")
	public ResponseEntity<PriceBreakdownDTO> getPlanBreakdown(
			@PathVariable Long planId,
			@RequestParam(required = false) BigDecimal discountAmount) {
		return ResponseEntity.ok(priceBreakdownService.calculateNewPlanBreakdown(planId, discountAmount));
	}

	@GetMapping("/member/{memberId}/upgrade/{planId}")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	@Operation(summary = "Get prorated breakdown for plan upgrade")
	public ResponseEntity<PriceBreakdownDTO> getUpgradeBreakdown(
			@PathVariable Long memberId,
			@PathVariable Long planId,
			@RequestParam(required = false) BigDecimal discountAmount) {
		return ResponseEntity
				.ok(priceBreakdownService.calculateUpgradeBreakdown(memberId, planId, discountAmount));
	}
}
