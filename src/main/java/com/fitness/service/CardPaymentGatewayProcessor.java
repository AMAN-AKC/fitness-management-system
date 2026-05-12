package com.fitness.service;

import com.fitness.dto.PaymentDTO;
import com.fitness.entity.Invoice;
import com.fitness.entity.Member;
import com.fitness.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentGatewayProcessor implements PaymentGatewayProcessor {

	@Override
	public boolean supports(Payment.PaymentMethod method) {
		return method == Payment.PaymentMethod.CARD;
	}

	@Override
	public PaymentProcessingResult process(PaymentDTO paymentDTO, Invoice invoice, Member member) {
		return PaymentProcessingResult.builder()
				.successful(true)
				.paymentStatus(Payment.PaymentStatus.SUCCESS)
				.gatewayReference("CARD-" + invoice.getInvoiceNumber() + "-" + System.nanoTime())
				.build();
	}
}