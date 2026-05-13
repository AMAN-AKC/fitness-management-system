package com.fitness.controller;

import com.fitness.dto.InvoiceDTO;
import com.fitness.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice management (US04)")
public class InvoiceController {

	private final InvoiceService invoiceService;

	@PostMapping
	@PreAuthorize("hasAnyRole('FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<InvoiceDTO> createInvoice(@Valid @RequestBody InvoiceDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.createInvoice(dto));
	}

	@GetMapping("/member/{memberId}")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<List<InvoiceDTO>> getInvoicesByMember(@PathVariable Long memberId) {
		return ResponseEntity.ok(invoiceService.getInvoicesByMember(memberId));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable Long id) {
		return ResponseEntity.ok(invoiceService.getInvoiceById(id));
	}

	/**
	 * AC09: Void an invoice — restricted to Manager/Admin with reason.
	 */
	@PatchMapping("/{id}/void")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Void an invoice (Manager/Admin only)")
	public ResponseEntity<InvoiceDTO> voidInvoice(@PathVariable Long id,
			@RequestParam String reason) {
		return ResponseEntity.ok(invoiceService.voidInvoice(id, reason));
	}
}
