package com.fitness.controller;

import com.fitness.entity.Invoice;
import com.fitness.service.InvoiceExportService;
import com.fitness.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoice Export", description = "Download invoices in PDF/CSV format (US03)")
public class InvoiceExportController {

	private final InvoiceExportService invoiceExportService;
	private final InvoiceService invoiceService;

	@GetMapping("/{invoiceId}/pdf")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	@Operation(summary = "Download invoice as PDF")
	public ResponseEntity<byte[]> downloadInvoicePDF(@PathVariable Long invoiceId)
			throws IOException {
		Invoice invoice = invoiceService.getInvoiceEntityById(invoiceId);
		byte[] pdfContent = invoiceExportService.exportInvoiceAsPDF(invoice);
		String filename = invoiceExportService.getInvoiceFileName(invoice, "pdf");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
				.contentType(MediaType.APPLICATION_PDF)
				.body(pdfContent);
	}

	@GetMapping("/{invoiceId}/csv")
	@PreAuthorize("hasAnyRole('MEMBER','FRONT_DESK','MANAGER','ADMIN')")
	@Operation(summary = "Download invoice as CSV")
	public ResponseEntity<byte[]> downloadInvoiceCSV(@PathVariable Long invoiceId)
			throws IOException {
		Invoice invoice = invoiceService.getInvoiceEntityById(invoiceId);
		byte[] csvContent = invoiceExportService.exportInvoiceAsCSV(invoice);
		String filename = invoiceExportService.getInvoiceFileName(invoice, "csv");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
				.contentType(MediaType.TEXT_PLAIN)
				.body(csvContent);
	}

	@GetMapping("/member/{memberId}/export/csv")
	@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
	@Operation(summary = "Download all invoices for a member as CSV")
	public ResponseEntity<byte[]> downloadMemberInvoicesCSV(@PathVariable Long memberId)
			throws IOException {
		List<Invoice> invoices = invoiceService.getInvoicesEntityByMember(memberId);
		byte[] csvContent = invoiceExportService.exportInvoicesAsCSV(invoices);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"Invoices_Member_" + memberId + ".csv\"")
				.contentType(MediaType.TEXT_PLAIN)
				.body(csvContent);
	}
}
