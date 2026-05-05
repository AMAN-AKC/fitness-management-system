package com.fitness.dto;

import com.fitness.entity.PtSession;
import com.fitness.validator.DateNotStartingWithZero;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PtSessionDTO {
	private Long sessionId;

	@NotNull(message = "Please provide a valid member")
	private Long memberId;

	@NotNull(message = "Please provide a valid trainer")
	private Long trainerId;

	@NotBlank(message = "Please provide a valid scheduled time")
	@DateNotStartingWithZero
	private String scheduledAt;

	@NotNull(message = "Please provide a valid duration")
	@Min(value = 30, message = "Session duration must be at least 30 minutes")
	private Integer durationMins;

	private PtSession.Status status;
	private String trainerNotes;
}