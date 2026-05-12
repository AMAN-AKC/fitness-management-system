package com.fitness.service;

import com.fitness.dto.PaymentDTO;
import com.fitness.entity.Invoice;
import com.fitness.entity.Member;
import com.fitness.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class UpiPaymentGatewayProcessor implements PaymentGatewayProcessor {

	@Override
	public boolean supports(Payment.PaymentMethod method) {
		return method == Payment.PaymentMethod.UPI;
	}

	@Override
	public PaymentProcessingResult process(PaymentDTO paymentDTO, Invoice invoice, Member member) {
		return PaymentProcessingResult.builder()
				.successful(true)
				.paymentStatus(Payment.PaymentStatus.SUCCESS)
				.gatewayReference("UPI-" + invoice.getInvoiceNumber() + "-" + System.currentTimeMillis())
				.build();
	}
}