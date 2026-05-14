package com.fitness.service;

import com.fitness.dto.ReceiptDTO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.fitness.repository.InvoiceRepository;
import com.fitness.repository.PaymentRepository;
import com.fitness.entity.AuditLog;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceiptService {

	private final ReceiptRepository receiptRepo;
	private final AuditLogService auditLogService;
	private final ModelMapper mapper;
	private final InvoiceRepository invoiceRepo;
	private final PaymentRepository paymentRepo;

	/**
	 * Generate receipt for a successful payment
	 */
	public ReceiptDTO generateReceipt(Long invoiceId, Long paymentId) {
		Invoice invoice = invoiceRepo.findById(invoiceId)
				.orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));
		Payment payment = paymentRepo.findById(paymentId)
				.orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

		if (payment.getPaymentStatus() != Payment.PaymentStatus.SUCCESS) {
			throw new BusinessRuleException("Receipt can only be generated for successful payments");
		}

		Receipt receipt = Receipt.builder()
				.receiptNumber("RCP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
				.invoice(invoice)
				.member(invoice.getMember())
				.payment(payment)
				.amount(payment.getAmountPaid())
				.taxAmount(invoice.getTaxes() != null ? invoice.getTaxes().multiply(
						payment.getAmountPaid().divide(invoice.getFinalAmount())) : java.math.BigDecimal.ZERO)
				.totalAmount(payment.getAmountPaid())
				.status(Receipt.Status.ISSUED)
				.build();

		receiptRepo.save(receipt);

		auditLogService.logForCurrentUser("Receipt", receipt.getReceiptId(), AuditLog.Action.CREATE,
				null, "Receipt generated for payment " + paymentId);

		return mapper.map(receipt, ReceiptDTO.class);
	}

	/**
	 * Mark receipt as emailed
	 */
	public ReceiptDTO markAsEmailed(Long receiptId) {
		Receipt receipt = receiptRepo.findById(receiptId)
				.orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));

		receipt.setStatus(Receipt.Status.EMAILED);
		receiptRepo.save(receipt);

		auditLogService.logForCurrentUser("Receipt", receiptId, AuditLog.Action.UPDATE,
				null, "Receipt marked as emailed to " + receipt.getMember().getEmail());

		return mapper.map(receipt, ReceiptDTO.class);
	}

	/**
	 * Get receipts by member
	 */
	public List<ReceiptDTO> getReceiptsByMember(Long memberId) {
		return receiptRepo.findByMemberMemberId(memberId).stream()
				.map(r -> mapper.map(r, ReceiptDTO.class)).collect(Collectors.toList());
	}

	/**
	 * Get receipt by receipt number
	 */
	public ReceiptDTO getReceiptByNumber(String receiptNumber) {
		return mapper.map(receiptRepo.findByReceiptNumber(receiptNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Receipt", "receiptNumber",
						receiptNumber)),
				ReceiptDTO.class);
	}
}
