package com.fitness.service;

import com.fitness.entity.Membership;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ProratedPriceService {

	/**
	 * Calculate remaining value of current membership based on days remaining
	 */
	public BigDecimal calculateRemainingValue(Membership membership) {
		LocalDate today = LocalDate.now();
		LocalDate endDate = membership.getEndDate();

		if (today.isAfter(endDate)) {
			return BigDecimal.ZERO; // Membership expired
		}

		long totalDays = membership.getDuration();
		long remainingDays = ChronoUnit.DAYS.between(today, endDate);

		BigDecimal pricePerDay = membership.getPrice().divide(new BigDecimal(totalDays), 4,
				RoundingMode.HALF_UP);
		BigDecimal remainingValue = pricePerDay.multiply(new BigDecimal(remainingDays));

		return remainingValue.setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * Calculate prorated price for partial membership
	 */
	public BigDecimal calculateProratedPrice(BigDecimal fullPrice, int daysUsed, int totalDays) {
		if (daysUsed <= 0 || totalDays <= 0) {
			return BigDecimal.ZERO;
		}

		BigDecimal pricePerDay = fullPrice.divide(new BigDecimal(totalDays), 4,
				RoundingMode.HALF_UP);
		return pricePerDay.multiply(new BigDecimal(daysUsed)).setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * Validate if prorated upgrade is valid (new plan is better value)
	 */
	public boolean isValidUpgrade(Membership currentMembership, BigDecimal newPlanPrice) {
		BigDecimal remainingValue = calculateRemainingValue(currentMembership);
		return newPlanPrice.compareTo(remainingValue) > 0;
	}
}
