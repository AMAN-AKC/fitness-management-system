package com.fitness.service;

import com.fitness.dto.InvoiceDTO;
import com.fitness.entity.*;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import com.fitness.entity.AuditLog;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

	private final InvoiceRepository invoiceRepo;
	private final MemberRepository memberRepo;
	private final ModelMapper mapper;
	private final AuditLogService auditLogService;

	public InvoiceDTO createInvoice(InvoiceDTO dto) {
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));
		Invoice invoice = mapper.map(dto, Invoice.class);
		invoice.setMember(member);
		if (dto.getMembershipId() != null) {
			Membership membership = new Membership();
			membership.setMemId(dto.getMembershipId());
			invoice.setMembership(membership);
		}
		invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		invoice.setOutstanding(dto.getFinalAmount().subtract(
				dto.getPaidAmount() != null ? dto.getPaidAmount() : BigDecimal.ZERO));
		invoice.setStatus(Invoice.Status.ISSUED);
		Invoice saved = invoiceRepo.save(invoice);
		auditLogService.logForCurrentUser("Invoice", saved.getInvoiceId(), AuditLog.Action.CREATE,
				null, "Invoice created for member: " + member.getMemName() + " | Amount: ₹" + saved.getFinalAmount());
		return mapper.map(saved, InvoiceDTO.class);
	}

	/**
	 * Mark invoice as void
	 */
	public InvoiceDTO voidInvoice(Long invoiceId, String reason) {
		Invoice invoice = invoiceRepo.findById(invoiceId)
				.orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

		if (invoice.getStatus() == Invoice.Status.PAID) {
			throw new com.fitness.exception.BusinessRuleException("Cannot void a paid invoice. Use refund instead.");
		}

		invoice.setStatus(Invoice.Status.VOID);
		Invoice saved = invoiceRepo.save(invoice);

		auditLogService.logForCurrentUser("Invoice", invoiceId, AuditLog.Action.UPDATE,
				null, "Invoice voided: " + reason);

		return mapper.map(saved, InvoiceDTO.class);
	}

	/**
	 * Return invoice entity for export/download use-cases
	 */
	public Invoice getInvoiceEntityById(Long id) {
		return invoiceRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
	}

	/**
	 * Return invoice entities for a member
	 */
	public List<Invoice> getInvoicesEntityByMember(Long memberId) {
		return invoiceRepo.findByMemberMemberId(memberId);
	}

	public List<InvoiceDTO> getInvoicesByMember(Long memberId) {
		return invoiceRepo.findByMemberMemberId(memberId).stream()
				.map(i -> mapper.map(i, InvoiceDTO.class)).collect(Collectors.toList());
	}

	public InvoiceDTO getInvoiceById(Long id) {
		return mapper.map(invoiceRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id)), InvoiceDTO.class);
	}
}