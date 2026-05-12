package com.fitness.service;

import com.fitness.entity.Invoice;
import com.fitness.exception.BusinessRuleException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceExportService {

	/**
	 * Export invoice as CSV format (stub for iText/Apache POI integration)
	 */
	public byte[] exportInvoiceAsCSV(Invoice invoice) throws IOException {
		if (invoice == null) {
			throw new BusinessRuleException("Invoice not found");
		}

		StringBuilder csv = new StringBuilder();
		csv.append("Invoice Number,Member Name,Plan Name,Base Amount,Taxes,Discount,Final Amount,");
		csv.append("Paid Amount,Outstanding,Status,Created Date\n");
		csv.append(invoice.getInvoiceNumber()).append(",");
		csv.append(invoice.getMember().getMemName()).append(",");
		csv.append(invoice.getMembership() != null ? invoice.getMembership().getPlan().getPlanName()
				: "N/A").append(",");
		csv.append(invoice.getMrp()).append(",");
		csv.append(invoice.getTaxes()).append(",");
		csv.append(invoice.getDiscount()).append(",");
		csv.append(invoice.getFinalAmount()).append(",");
		csv.append(invoice.getPaidAmount()).append(",");
		csv.append(invoice.getOutstanding()).append(",");
		csv.append(invoice.getStatus()).append(",");
		csv.append(invoice.getCreatedAt()).append("\n");

		return csv.toString().getBytes();
	}

	/**
	 * Export multiple invoices as CSV
	 */
	public byte[] exportInvoicesAsCSV(List<Invoice> invoices) throws IOException {
		StringBuilder csv = new StringBuilder();
		csv.append(
				"Invoice Number,Member Name,Plan Name,Base Amount,Taxes,Discount,Final Amount,Paid Amount,Outstanding,Status,Created Date\n");

		for (Invoice inv : invoices) {
			csv.append(inv.getInvoiceNumber()).append(",");
			csv.append(inv.getMember().getMemName()).append(",");
			csv.append(inv.getMembership() != null
					? inv.getMembership().getPlan().getPlanName()
					: "N/A").append(",");
			csv.append(inv.getMrp()).append(",");
			csv.append(inv.getTaxes()).append(",");
			csv.append(inv.getDiscount()).append(",");
			csv.append(inv.getFinalAmount()).append(",");
			csv.append(inv.getPaidAmount()).append(",");
			csv.append(inv.getOutstanding()).append(",");
			csv.append(inv.getStatus()).append(",");
			csv.append(inv.getCreatedAt()).append("\n");
		}

		return csv.toString().getBytes();
	}

	/**
	 * Export invoice as PDF.
	 */
	public byte[] exportInvoiceAsPDF(Invoice invoice) throws IOException {
		if (invoice == null) {
			throw new BusinessRuleException("Invoice not found");
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			Document document = new Document();
			PdfWriter.getInstance(document, outputStream);
			document.open();

			document.add(new Paragraph("Fitness Management System"));
			document.add(new Paragraph("Invoice: " + invoice.getInvoiceNumber()));
			document.add(new Paragraph("Member: " + invoice.getMember().getMemName()));
			document.add(new Paragraph("Invoice Date: " + invoice.getCreatedAt()));
			document.add(new Paragraph(" "));

			PdfPTable table = new PdfPTable(2);
			table.setWidthPercentage(100);
			table.addCell(new PdfPCell(new Phrase("Field")));
			table.addCell(new PdfPCell(new Phrase("Value")));
			table.addCell("Plan Name");
			table.addCell(invoice.getMembership() != null ? invoice.getMembership().getPlan().getPlanName() : "N/A");
			table.addCell("Base Amount");
			table.addCell(String.valueOf(invoice.getMrp()));
			table.addCell("Taxes");
			table.addCell(String.valueOf(invoice.getTaxes()));
			table.addCell("Discount");
			table.addCell(String.valueOf(invoice.getDiscount()));
			table.addCell("Final Amount");
			table.addCell(String.valueOf(invoice.getFinalAmount()));
			table.addCell("Paid Amount");
			table.addCell(String.valueOf(invoice.getPaidAmount()));
			table.addCell("Outstanding");
			table.addCell(String.valueOf(invoice.getOutstanding()));
			table.addCell("Status");
			table.addCell(String.valueOf(invoice.getStatus()));

			document.add(table);
			document.close();
			return outputStream.toByteArray();
		} catch (DocumentException ex) {
			throw new IOException("Failed to generate PDF invoice", ex);
		}
	}

	/**
	 * Get filename for invoice export
	 */
	public String getInvoiceFileName(Invoice invoice, String format) {
		return "Invoice_" + invoice.getInvoiceNumber() + "." + format.toLowerCase();
	}
}
