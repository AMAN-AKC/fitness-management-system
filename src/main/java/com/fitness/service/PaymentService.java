package com.fitness.service;

import com.fitness.dto.PaymentDTO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepo;
	private final InvoiceRepository invoiceRepo;
	private final MemberRepository memberRepo;
	private final SystemUserRepository userRepo;
	private final ModelMapper mapper;

	public PaymentDTO processPayment(PaymentDTO dto) {
		Invoice invoice = invoiceRepo.findById(dto.getInvoiceId())
				.orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", dto.getInvoiceId()));
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));

		Payment payment = mapper.map(dto, Payment.class);
		payment.setInvoice(invoice);
		payment.setMember(member);
		payment.setPaymentDate(LocalDateTime.now());
		payment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);

		invoice.setPaidAmount(invoice.getPaidAmount().add(dto.getAmountPaid()));
		invoice.setOutstanding(invoice.getFinalAmount().subtract(invoice.getPaidAmount()));
		if (invoice.getOutstanding().compareTo(java.math.BigDecimal.ZERO) <= 0)
			invoice.setStatus(Invoice.Status.PAID);
		invoiceRepo.save(invoice);

		return mapper.map(paymentRepo.save(payment), PaymentDTO.class);
	}

	public PaymentDTO refundPayment(Long paymentId, Long refundByUserId, String reason) {
		Payment payment = paymentRepo.findById(paymentId)
				.orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
		if (payment.getPaymentStatus() != Payment.PaymentStatus.SUCCESS)
			throw new BusinessRuleException("Only successful payments can be refunded.");
		SystemUser refundUser = userRepo.findById(refundByUserId)
				.orElseThrow(() -> new ResourceNotFoundException("SystemUser", "id", refundByUserId));
		payment.setPaymentStatus(Payment.PaymentStatus.REFUNDED);
		payment.setRefundBy(refundUser);
		payment.setRefundReason(reason);
		return mapper.map(paymentRepo.save(payment), PaymentDTO.class);
	}

	public List<PaymentDTO> getPaymentsByMember(Long memberId) {
		return paymentRepo.findByMemberMemberId(memberId).stream()
				.map(p -> mapper.map(p, PaymentDTO.class)).collect(Collectors.toList());
	}

	public List<PaymentDTO> getFailedPayments() {
		return paymentRepo.findByPaymentStatus(Payment.PaymentStatus.FAILED).stream()
				.map(p -> mapper.map(p, PaymentDTO.class)).collect(Collectors.toList());
	}
}