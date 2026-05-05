package com.fitness.dto;

import com.fitness.entity.AddOn;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddOnDTO {
	private Long addonId;

	@NotBlank(message = "Please provide a valid add-on name")
	private String addonName;

	@NotNull(message = "Please provide a valid price")
	@DecimalMin(value = "0.0", inclusive = false, message = "Please provide a valid price")
	private BigDecimal price;

	private Integer capacity;

	@NotNull(message = "Please provide a valid add-on type")
	private AddOn.AddonType addonType;

	private BigDecimal taxPercent;
	private Boolean isActive;
}