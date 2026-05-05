package com.fitness.dto;

import com.fitness.entity.HealthConsent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthConsentDTO {
	private Long consentId;

	@NotNull(message = "Please provide a valid member")
	private Long memberId;

	@NotBlank(message = "Please provide a valid form version")
	private String formVersion;

	private String acknowledgedAt;
	private String ipAddress;
	private HealthConsent.Status status;
	private String staffNotes;
}
