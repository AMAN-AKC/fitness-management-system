package com.fitness.dto;

import com.fitness.entity.Invoice;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDTO {
	private Long invoiceId;
	private String invoiceNumber;
	private String planName;

	@NotNull(message = "Please provide a valid member")
	private Long memberId;

	private Long membershipId;
	private BigDecimal mrp;
	private BigDecimal taxes;
	private BigDecimal discount;
	private BigDecimal finalAmount;
	private BigDecimal paidAmount;
	private BigDecimal outstanding;
	private String promoCode;
	private Invoice.Status status;
	private String createdAt;
}