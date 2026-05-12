package com.fitness.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceBreakdownDTO {
	private Long planId;
	private String planName;
	private Integer durationDays;
	private BigDecimal basePrice;
	private BigDecimal discount;
	private BigDecimal priceAfterDiscount;
	private BigDecimal taxPercent;
	private BigDecimal taxAmount;
	private BigDecimal finalAmount;
	private ProratedPrice proration;
	private String type; // NEW_PLAN or UPGRADE

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ProratedPrice {
		private String currentPlanName;
		private Integer remainingDays;
		private BigDecimal remainingValue;
		private BigDecimal creditApplied;
	}
}
