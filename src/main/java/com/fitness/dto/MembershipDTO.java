package com.fitness.dto;

import com.fitness.entity.Membership;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipDTO {
	private Long memId;

	@NotNull(message = "Please provide a valid member")
	private Long memberId;

	@NotNull(message = "Please provide a valid plan")
	private Long planId;

	private String startDate;
	private String endDate;
	private Membership.Status status;
	private Integer duration;
	private BigDecimal price;
	private BigDecimal discountAmount;
	private String promoCodeUsed;
	private Long branchId;
}