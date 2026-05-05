package com.fitness.dto;

import com.fitness.entity.Plan;
import com.fitness.validator.DateNotStartingWithZero;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDTO {
	private Long planId;

	@NotBlank(message = "Please provide a valid plan name")
	private String planName;

	@NotNull(message = "Please provide a valid duration")
	@Min(value = 1, message = "Duration must be at least 1 day")
	private Integer durationDays;

	@NotNull(message = "Please provide a valid price")
	@DecimalMin(value = "0.0", inclusive = false, message = "Please provide a valid price")
	private BigDecimal price;

	@NotBlank(message = "Please provide a valid access start time")
	@DateNotStartingWithZero
	private String accessStart; // "HH:mm"

	@NotBlank(message = "Please provide a valid access end time")
	@DateNotStartingWithZero
	private String accessEnd;

	private Plan.EligibilityType eligibilityType;
	private String prorationRule;

	@DecimalMin(value = "0.0", message = "Please provide a valid tax percent")
	private BigDecimal taxPercent;

	private Integer version;

	@NotBlank(message = "Please provide a valid effective from date")
	@DateNotStartingWithZero
	private String effectiveFrom;

	private String branchVisibility;
	private Boolean isActive;
}