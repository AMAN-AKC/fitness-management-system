package com.fitness.service;

import com.fitness.dto.PriceBreakdownDTO;
import com.fitness.entity.Plan;
import com.fitness.entity.Membership;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.PlanRepository;
import com.fitness.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PriceBreakdownService {

	private final PlanRepository planRepo;
	private final MembershipRepository membershipRepo;
	private final ProratedPriceService proratedPriceService;

	/**
	 * Generate price breakdown for a new plan purchase
	 */
	public PriceBreakdownDTO calculateNewPlanBreakdown(Long planId, BigDecimal discountAmount) {
		Plan plan = planRepo.findById(planId)
				.orElseThrow(() -> new ResourceNotFoundException("Plan", "id", planId));

		BigDecimal basePrice = plan.getPrice();
		BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
		BigDecimal priceAfterDiscount = basePrice.subtract(discount);
		BigDecimal taxAmount = calculateTax(priceAfterDiscount, plan.getTaxPercent());
		BigDecimal finalAmount = priceAfterDiscount.add(taxAmount);

		return PriceBreakdownDTO.builder()
				.planId(planId)
				.planName(plan.getPlanName())
				.durationDays(plan.getDurationDays())
				.basePrice(basePrice)
				.discount(discount)
				.priceAfterDiscount(priceAfterDiscount)
				.taxPercent(plan.getTaxPercent())
				.taxAmount(taxAmount)
				.finalAmount(finalAmount)
				.proration(null)
				.type("NEW_PLAN")
				.build();
	}

	/**
	 * Generate price breakdown for plan upgrade with proration
	 */
	public PriceBreakdownDTO calculateUpgradeBreakdown(Long memberId, Long newPlanId,
			BigDecimal discountAmount) {
		List<Membership> activeList = membershipRepo.findByMemberMemberIdAndStatus(memberId,
				Membership.Status.ACTIVE);
		if (activeList.isEmpty()) {
			throw new ResourceNotFoundException("Active membership", "memberId", memberId);
		}
		Membership currentMembership = activeList.get(0);

		Plan currentPlan = currentMembership.getPlan();
		Plan newPlan = planRepo.findById(newPlanId)
				.orElseThrow(() -> new ResourceNotFoundException("Plan", "id", newPlanId));

		BigDecimal remainingValue = proratedPriceService.calculateRemainingValue(currentMembership);
		BigDecimal newPlanPrice = newPlan.getPrice();
		BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;

		// Upgrade cost = new plan price - (remaining value of old plan) - discount
		BigDecimal upgradeCost = newPlanPrice.subtract(remainingValue).subtract(discount);
		upgradeCost = upgradeCost.max(BigDecimal.ZERO); // Ensure non-negative

		BigDecimal taxAmount = calculateTax(upgradeCost, newPlan.getTaxPercent());
		BigDecimal finalAmount = upgradeCost.add(taxAmount);

		return PriceBreakdownDTO.builder()
				.planId(newPlanId)
				.planName(newPlan.getPlanName())
				.durationDays(newPlan.getDurationDays())
				.basePrice(newPlanPrice)
				.discount(discount)
				.priceAfterDiscount(upgradeCost)
				.taxPercent(newPlan.getTaxPercent())
				.taxAmount(taxAmount)
				.finalAmount(finalAmount)
				.proration(PriceBreakdownDTO.ProratedPrice.builder()
						.currentPlanName(currentPlan.getPlanName())
						.remainingDays((int) ChronoUnit.DAYS.between(LocalDate.now(),
								currentMembership.getEndDate()))
						.remainingValue(remainingValue)
						.creditApplied(remainingValue)
						.build())
				.type("UPGRADE")
				.build();
	}

	/**
	 * Calculate tax on a given amount
	 */
	private BigDecimal calculateTax(BigDecimal amount, BigDecimal taxPercent) {
		if (taxPercent == null || taxPercent.equals(BigDecimal.ZERO)) {
			return BigDecimal.ZERO;
		}
		return amount.multiply(taxPercent).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
	}
}
