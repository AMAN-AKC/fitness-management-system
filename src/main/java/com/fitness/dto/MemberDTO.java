package com.fitness.dto;

import com.fitness.entity.Member;
import com.fitness.validator.DateNotStartingWithZero;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
	private Long memberId;

	@NotBlank(message = "Please provide a valid name")
	@Size(max = 120, message = "Name must not exceed 120 characters")
	private String memName;

	@NotBlank(message = "Please provide a valid email")
	@Email(message = "Please provide a valid email")
	private String email;

	@NotBlank(message = "Please provide a valid phone")
	@Pattern(regexp = "^[6-9]\\d{9}$", message = "Please provide a valid phone")
	private String phone;

	@NotBlank(message = "Please provide a valid date of birth")
	@DateNotStartingWithZero
	private String dob; // "yyyy-MM-dd"

	@NotBlank(message = "Please provide a valid address")
	private String address;

	@NotBlank(message = "Please provide a valid emergency contact")
	private String emgContact;

	@NotBlank(message = "Please provide a valid emergency phone")
	@Pattern(regexp = "^[6-9]\\d{9}$", message = "Please provide a valid emergency phone")
	private String emgPhone;

	private String referralCode;
	private String corporateCode;
	private String notes;
	private Member.Status status;

	@NotNull(message = "Please provide a valid home branch")
	private Long homeBranchId;

	private String photoPath;
}
