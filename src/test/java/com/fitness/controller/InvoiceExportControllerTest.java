package com.fitness.controller;

import com.fitness.entity.Invoice;
import com.fitness.service.InvoiceExportService;
import com.fitness.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceExportController.class)
@AutoConfigureMockMvc(addFilters = false)
public class InvoiceExportControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private InvoiceExportService invoiceExportService;
    @MockBean
    private InvoiceService invoiceService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void downloadInvoicePDF_Success() throws Exception {
        Invoice invoice = new Invoice();
        when(invoiceService.getInvoiceEntityById(10L)).thenReturn(invoice);
        when(invoiceExportService.exportInvoiceAsPDF(invoice)).thenReturn(new byte[]{1, 2, 3});
        when(invoiceExportService.getInvoiceFileName(invoice, "pdf")).thenReturn("Invoice_10.pdf");

        mockMvc.perform(get("/api/v1/invoices/10/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"Invoice_10.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void downloadInvoiceCSV_Success() throws Exception {
        Invoice invoice = new Invoice();
        when(invoiceService.getInvoiceEntityById(10L)).thenReturn(invoice);
        when(invoiceExportService.exportInvoiceAsCSV(invoice)).thenReturn("csv_data".getBytes());
        when(invoiceExportService.getInvoiceFileName(invoice, "csv")).thenReturn("Invoice_10.csv");

        mockMvc.perform(get("/api/v1/invoices/10/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"Invoice_10.csv\""))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().bytes("csv_data".getBytes()));
    }

    @Test
    void downloadMemberInvoicesCSV_Success() throws Exception {
        Invoice invoice = new Invoice();
        when(invoiceService.getInvoicesEntityByMember(1L)).thenReturn(Collections.singletonList(invoice));
        when(invoiceExportService.exportInvoicesAsCSV(any())).thenReturn("csv_data".getBytes());

        mockMvc.perform(get("/api/v1/invoices/member/1/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"Invoices_Member_1.csv\""))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().bytes("csv_data".getBytes()));
    }
}
