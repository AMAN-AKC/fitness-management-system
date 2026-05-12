package com.fitness.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanAddOnDTO {
	private Long id;

	@NotNull(message = "Plan ID is required")
	private Long planId;

	@NotNull(message = "Add-on ID is required")
	private Long addonId;

	private Boolean isIncluded;
}
