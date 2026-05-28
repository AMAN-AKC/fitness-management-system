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

	@NotBlank(message = "Please provide a valid Full Name")
	@Pattern(regexp = "^[a-zA-Z\\s]{2,70}$", message = "Please provide a valid Full Name")
	private String memName;

	@NotBlank(message = "Please provide a valid Email Address")
	@Email(message = "Please provide a valid Email Address")
	private String email;

	@NotBlank(message = "Please provide a valid Phone Number")
	@Pattern(regexp = "^\\d{10,15}$", message = "Please provide a valid Phone Number")
	private String phone;

	@NotBlank(message = "Please provide a valid Date of Birth")
	@DateNotStartingWithZero
	private String dob; // "yyyy-MM-dd"

	@NotBlank(message = "Please provide a valid Physical Address")
	private String address;

	@NotBlank(message = "Please provide a valid Emergency Contact")
	private String emgContact;

	@NotBlank(message = "Please provide a valid Emergency Contact")
	@Pattern(regexp = "^\\d{10,15}$", message = "Please provide a valid Emergency Contact")
	private String emgPhone;

	private String referralCode;
	private String corporateCode;
	private String myReferralCode;
	private java.math.BigDecimal walletBalance;
	private String notes;
	private Member.Status status;

	@NotNull(message = "Please provide a valid home branch")
	private Long homeBranchId;

	private String photoPath;
	private Integer ptSessionCredits;
}
