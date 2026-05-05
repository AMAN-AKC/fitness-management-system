package com.fitness.dto;

import com.fitness.entity.PromoCode;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCodeDTO {
	private Long promoId;

	@NotBlank(message = "Please provide a valid promo code")
	private String code;

	@NotNull(message = "Please provide a valid discount type")
	private PromoCode.DiscountType discountType;

	@NotNull(message = "Please provide a valid discount value")
	@DecimalMin(value = "0.01", message = "Please provide a valid discount value")
	private BigDecimal discountValue;

	@NotBlank(message = "Please provide a valid expiry date")
	private String expiryDate;

	@NotNull(message = "Please provide a valid usage limit")
	@Min(value = 1, message = "Usage limit must be at least 1")
	private Integer usageLimit;

	private Integer perMemberLimit;
	private PromoCode.Eligibility eligibility;
	private Boolean isActive;
}
