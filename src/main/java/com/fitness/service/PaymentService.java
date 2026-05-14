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
import com.fitness.entity.AuditLog;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepo;
	private final InvoiceRepository invoiceRepo;
	private final MemberRepository memberRepo;
	private final SystemUserRepository userRepo;
	private final ModelMapper mapper;
	private final AuditLogService auditLogService;
	private final ReceiptService receiptService;
	private final EmailService emailService;
	private final DunningService dunningService;
	private final List<PaymentGatewayProcessor> paymentGatewayProcessors;

	public PaymentDTO processPayment(PaymentDTO dto) {
		Invoice invoice = invoiceRepo.findById(dto.getInvoiceId())
				.orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", dto.getInvoiceId()));
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));
		PaymentGatewayProcessor processor = paymentGatewayProcessors.stream()
				.filter(candidate -> candidate.supports(dto.getPaymentMethod()))
				.findFirst()
				.orElseThrow(() -> new BusinessRuleException("Unsupported payment method: " + dto.getPaymentMethod()));
		PaymentProcessingResult paymentResult = processor.process(dto, invoice, member);

		Payment payment = mapper.map(dto, Payment.class);
		payment.setInvoice(invoice);
		payment.setMember(member);
		payment.setPaymentDate(LocalDateTime.now());
		payment.setPaymentStatus(paymentResult.getPaymentStatus());
		payment.setFailureReason(paymentResult.getFailureReason());

		if (paymentResult.isSuccessful()) {
			invoice.setPaidAmount(invoice.getPaidAmount().add(dto.getAmountPaid()));
			invoice.setOutstanding(invoice.getFinalAmount().subtract(invoice.getPaidAmount()));
			if (invoice.getOutstanding().compareTo(java.math.BigDecimal.ZERO) <= 0) {
				invoice.setStatus(Invoice.Status.PAID);
			}
		} else {
			invoice.setStatus(Invoice.Status.OVERDUE);
		}
		invoiceRepo.save(invoice);

		// Save payment
		Payment savedPayment = paymentRepo.save(payment);

		if (!paymentResult.isSuccessful()) {
			dunningService.handleFailedPayment(invoice.getInvoiceId(), paymentResult.getFailureReason());
			auditLogService.logForCurrentUser("Payment", savedPayment.getPaymentId(), AuditLog.Action.UPDATE,
					null, "Payment failed: " + paymentResult.getFailureReason());
			return mapper.map(savedPayment, PaymentDTO.class);
		}

		// Generate receipt (DTO)
		com.fitness.dto.ReceiptDTO receipt = receiptService.generateReceipt(invoice.getInvoiceId(),
				savedPayment.getPaymentId());

		// Send email receipt (by DTO)
		emailService.sendReceiptEmail(receipt);

		// Audit log
		auditLogService.logForCurrentUser("Payment", savedPayment.getPaymentId(), AuditLog.Action.CREATE,
				null, "Payment processed: " + dto.getAmountPaid() + " via " + dto.getPaymentMethod());

		// Resolve dunning if any
		if (invoice.getMembership() != null) {
			dunningService.resolveDunning(invoice.getMembership().getMemId());
		}

		return mapper.map(savedPayment, PaymentDTO.class);
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
		auditLogService.logForCurrentUser("Payment", paymentId, AuditLog.Action.UPDATE,
				null, "Refunded by " + refundUser.getUsername() + ": " + reason);
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

	public BigDecimal getRevenueMTD() {
		LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
		BigDecimal revenue = paymentRepo.sumRevenueSince(startOfMonth);
		return revenue != null ? revenue : BigDecimal.ZERO;
	}
}