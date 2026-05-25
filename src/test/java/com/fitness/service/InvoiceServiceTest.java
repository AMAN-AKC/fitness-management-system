package com.fitness.service;

import com.fitness.dto.InvoiceDTO;
import com.fitness.entity.Invoice;
import com.fitness.entity.Member;
import com.fitness.exception.BusinessRuleException;
import com.fitness.repository.InvoiceRepository;
import com.fitness.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvoiceServiceTest {

    @InjectMocks
    private InvoiceService invoiceService;

    @Mock
    private InvoiceRepository invoiceRepo;

    @Mock
    private MemberRepository memberRepo;

    @Mock
    private ModelMapper mapper;

    @Mock
    private AuditLogService auditLogService;

    @Test
    void createInvoice_Success() {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setMemberId(1L);
        dto.setFinalAmount(BigDecimal.TEN);

        Member member = new Member();
        member.setMemberId(1L);

        Invoice invoice = new Invoice();
        invoice.setInvoiceId(1L);
        invoice.setFinalAmount(BigDecimal.TEN);

        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(mapper.map(dto, Invoice.class)).thenReturn(invoice);
        when(invoiceRepo.save(any(Invoice.class))).thenReturn(invoice);
        when(mapper.map(any(), eq(InvoiceDTO.class))).thenReturn(dto);

        InvoiceDTO result = invoiceService.createInvoice(dto);

        assertNotNull(result);
        assertEquals(Invoice.Status.ISSUED, invoice.getStatus());
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
    }

    @Test
    void voidInvoice_PaidInvoice_ThrowsException() {
        Invoice invoice = new Invoice();
        invoice.setStatus(Invoice.Status.PAID);

        when(invoiceRepo.findById(1L)).thenReturn(Optional.of(invoice));

        assertThrows(BusinessRuleException.class, () -> invoiceService.voidInvoice(1L, "Mistake"));
    }

    @Test
    void voidInvoice_Success() {
        Invoice invoice = new Invoice();
        invoice.setStatus(Invoice.Status.ISSUED);

        when(invoiceRepo.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepo.save(any())).thenReturn(invoice);
        when(mapper.map(any(), eq(InvoiceDTO.class))).thenReturn(new InvoiceDTO());

        InvoiceDTO result = invoiceService.voidInvoice(1L, "Mistake");

        assertNotNull(result);
        assertEquals(Invoice.Status.VOID, invoice.getStatus());
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
    }
}
