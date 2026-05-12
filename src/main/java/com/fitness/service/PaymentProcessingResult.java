package com.fitness.service;

import com.fitness.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProcessingResult {

	private boolean successful;
	private Payment.PaymentStatus paymentStatus;
	private String failureReason;
	private String gatewayReference;
}