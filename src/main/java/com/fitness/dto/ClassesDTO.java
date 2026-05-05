package com.fitness.dto;

import com.fitness.entity.Classes;
import com.fitness.validator.DateNotStartingWithZero;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassesDTO {
	private Long classId;

	@NotBlank(message = "Please provide a valid class name")
	private String classesName;

	@NotNull(message = "Please provide a valid trainer")
	private Long trainerId;

	@NotNull(message = "Please provide a valid room")
	private Long roomId;

	@NotNull(message = "Please provide a valid branch")
	private Long branchId;

	@NotBlank(message = "Please provide a valid start date")
	@DateNotStartingWithZero
	private String startDate;

	@NotBlank(message = "Please provide a valid end date")
	@DateNotStartingWithZero
	private String endDate;

	@NotBlank(message = "Please provide a valid weekdays")
	private String weekdays;

	@NotBlank(message = "Please provide a valid class time")
	@DateNotStartingWithZero
	private String classTime;

	@NotNull(message = "Please provide a valid duration")
	@Min(value = 15, message = "Duration must be at least 15 minutes")
	private Integer durationMins;

	@NotNull(message = "Please provide a valid capacity")
	@Min(value = 1, message = "Capacity must be at least 1")
	private Integer capacity;

	private String prerequisites;
	private String planEligibility;
	private Classes.Status status;
	private String cancelReason;
}