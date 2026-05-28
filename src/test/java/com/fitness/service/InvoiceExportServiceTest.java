package com.fitness.service;

import com.fitness.entity.Invoice;
import com.fitness.entity.Member;
import com.fitness.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class InvoiceExportServiceTest {

    @InjectMocks
    private InvoiceExportService invoiceExportService;

    private Invoice mockInvoice;

    @BeforeEach
    void setUp() {
        Member member = new Member();
        member.setMemName("John Doe");

        mockInvoice = new Invoice();
        mockInvoice.setInvoiceNumber("INV-1234");
        mockInvoice.setMember(member);
        mockInvoice.setPlanName("Gold Plan");
        mockInvoice.setMrp(BigDecimal.valueOf(100));
        mockInvoice.setTaxes(BigDecimal.valueOf(10));
        mockInvoice.setDiscount(BigDecimal.valueOf(5));
        mockInvoice.setFinalAmount(BigDecimal.valueOf(105));
        mockInvoice.setPaidAmount(BigDecimal.valueOf(105));
        mockInvoice.setOutstanding(BigDecimal.ZERO);
        mockInvoice.setStatus(Invoice.Status.PAID);
    }

    @Test
    void exportInvoiceAsCSV_Success() throws IOException {
        byte[] result = invoiceExportService.exportInvoiceAsCSV(mockInvoice);
        assertNotNull(result);
        String csvString = new String(result);
        assertTrue(csvString.contains("INV-1234"));
        assertTrue(csvString.contains("John Doe"));
        assertTrue(csvString.contains("Gold Plan"));
    }

    @Test
    void exportInvoiceAsCSV_NullInvoice_ThrowsException() {
        assertThrows(BusinessRuleException.class, () -> invoiceExportService.exportInvoiceAsCSV(null));
    }

    @Test
    void exportInvoicesAsCSV_Success() throws IOException {
        byte[] result = invoiceExportService.exportInvoicesAsCSV(Collections.singletonList(mockInvoice));
        assertNotNull(result);
        String csvString = new String(result);
        assertTrue(csvString.contains("INV-1234"));
    }

    @Test
    void exportInvoiceAsPDF_Success() throws IOException {
        byte[] result = invoiceExportService.exportInvoiceAsPDF(mockInvoice);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void exportInvoiceAsPDF_NullInvoice_ThrowsException() {
        assertThrows(BusinessRuleException.class, () -> invoiceExportService.exportInvoiceAsPDF(null));
    }

    @Test
    void getInvoiceFileName_Success() {
        String filename = invoiceExportService.getInvoiceFileName(mockInvoice, "PDF");
        assertEquals("Invoice_INV-1234.pdf", filename);
    }
}
