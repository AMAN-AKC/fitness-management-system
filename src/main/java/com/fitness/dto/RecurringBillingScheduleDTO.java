package com.fitness.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringBillingScheduleDTO {
	private Long membershipId;
	private Long memberId;
	private Long planId;
	private boolean isRecurring;
	private LocalDate nextBillingDate;
	private String frequency; // MONTHLY, QUARTERLY, ANNUAL
	private BigDecimal amount;
	private String status; // ACTIVE, PAUSED, CANCELLED, NOT_ELIGIBLE
	private String reason; // For non-eligible schedules
	private LocalDate createdAt;
	private String paymentMethod;
}
