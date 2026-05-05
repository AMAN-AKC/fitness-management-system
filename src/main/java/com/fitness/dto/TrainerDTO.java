package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerDTO {
	private Long trainerId;

	@NotNull(message = "Please provide a valid user")
	private Long userId;

	@NotBlank(message = "Please provide a valid trainer name")
	private String trainerName;

	private String bio;
	private String certifications;
	private String specialties;
	private BigDecimal rating;

	@NotNull(message = "Please provide a valid branch")
	private Long branchId;

	private Boolean isActive;
}