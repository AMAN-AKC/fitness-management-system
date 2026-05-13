package com.fitness.dto;

import com.fitness.entity.HealthConsent;
import jakarta.validation.constraints.AssertTrue;
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

	@NotBlank(message = "PAR-Q questionnaire responses are required")
	private String parqResponses;

	@AssertTrue(message = "Medical acknowledgement is required")
	private Boolean medicalAcknowledged;

	@AssertTrue(message = "Liability waiver acknowledgement is required")
	private Boolean liabilityAcknowledged;

	@AssertTrue(message = "Privacy acknowledgement is required")
	private Boolean privacyAcknowledged;

	private String acknowledgedAt;
	private String expiresAt;
	private String ipAddress;
	private HealthConsent.Status status;
	private String staffNotes;
	private Boolean requiresReconfirmation;
	private Boolean consentRequired;
}
