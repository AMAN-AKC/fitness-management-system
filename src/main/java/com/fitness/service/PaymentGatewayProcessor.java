package com.fitness.service;

import com.fitness.dto.PaymentDTO;
import com.fitness.entity.Invoice;
import com.fitness.entity.Member;
import com.fitness.entity.Payment;

public interface PaymentGatewayProcessor {

	boolean supports(Payment.PaymentMethod method);

	PaymentProcessingResult process(PaymentDTO paymentDTO, Invoice invoice, Member member);
}