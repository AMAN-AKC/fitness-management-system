package com.fitness.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityDTO {
	private Long facilityId;

	@NotBlank(message = "Please provide a valid facility name")
	private String facilityName;

	@NotNull(message = "Please provide a valid branch")
	private Long branchId;

	@NotNull(message = "Please provide a valid capacity")
	@Min(value = 1, message = "Capacity must be at least 1")
	private Integer capacity;

	private Boolean isActive;
	private Boolean underMaintenance;
	private String maintenanceReason;
}
