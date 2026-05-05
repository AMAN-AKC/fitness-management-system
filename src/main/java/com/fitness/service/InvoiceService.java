package com.fitness.service;

import com.fitness.dto.InvoiceDTO;
import com.fitness.entity.*;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
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

	public InvoiceDTO createInvoice(InvoiceDTO dto) {
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));
		Invoice invoice = mapper.map(dto, Invoice.class);
		invoice.setMember(member);
		invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		invoice.setOutstanding(dto.getFinalAmount().subtract(
				dto.getPaidAmount() != null ? dto.getPaidAmount() : BigDecimal.ZERO));
		invoice.setStatus(Invoice.Status.ISSUED);
		return mapper.map(invoiceRepo.save(invoice), InvoiceDTO.class);
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