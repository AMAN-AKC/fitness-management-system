package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchDTO {
	private Long branchId;

	@NotBlank(message = "Please provide a valid branch name")
	@Size(max = 120)
	private String branchName;

	@NotBlank(message = "Please provide a valid address")
	private String address;

	@NotBlank(message = "Please provide a valid contact")
	private String contact;

	@NotBlank(message = "Please provide a valid operating hours")
	private String opHours;

	@NotBlank(message = "Please provide a valid timezone")
	private String timezone;

	private Boolean isActive;
}