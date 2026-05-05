package com.fitness.dto;

import com.fitness.entity.Payment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
	private Long paymentId;

	@NotNull(message = "Please provide a valid invoice")
	private Long invoiceId;

	@NotNull(message = "Please provide a valid member")
	private Long memberId;

	@NotNull(message = "Please provide a valid payment method")
	private Payment.PaymentMethod paymentMethod;

	@NotNull(message = "Please provide a valid amount")
	@DecimalMin(value = "0.01", message = "Please provide a valid amount")
	private BigDecimal amountPaid;

	private String paymentDate;
	private Payment.PaymentStatus paymentStatus;
	private String failureReason;
	private Long refundBy;
	private String refundReason;
}