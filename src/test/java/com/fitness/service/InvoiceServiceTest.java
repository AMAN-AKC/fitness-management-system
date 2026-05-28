package com.fitness.service;

import com.fitness.dto.InvoiceDTO;
import com.fitness.entity.AuditLog;
import com.fitness.entity.Invoice;
import com.fitness.entity.Member;
import com.fitness.entity.Membership;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.InvoiceRepository;
import com.fitness.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private AuditLogService auditLogService;
    @Mock
    private ModelMapper mapper;

    private Member mockMember;
    private Invoice mockInvoice;
    private InvoiceDTO mockDto;

    @BeforeEach
    void setUp() {
        mockMember = new Member();
        mockMember.setMemberId(1L);
        mockMember.setMemName("Jane Doe");

        mockInvoice = new Invoice();
        mockInvoice.setInvoiceId(10L);
        mockInvoice.setFinalAmount(BigDecimal.valueOf(100));
        mockInvoice.setStatus(Invoice.Status.ISSUED);
        mockInvoice.setMember(mockMember);

        mockDto = new InvoiceDTO();
        mockDto.setInvoiceId(10L);
        mockDto.setMemberId(1L);
        mockDto.setMembershipId(20L);
        mockDto.setFinalAmount(BigDecimal.valueOf(100));
        mockDto.setPaidAmount(BigDecimal.valueOf(20));
    }

    @Test
    void createInvoice_Success() {
        when(memberRepo.findById(1L)).thenReturn(Optional.of(mockMember));
        when(mapper.map(any(InvoiceDTO.class), eq(Invoice.class))).thenReturn(mockInvoice);
        when(invoiceRepo.save(any(Invoice.class))).thenReturn(mockInvoice);
        when(mapper.map(any(Invoice.class), eq(InvoiceDTO.class))).thenReturn(mockDto);

        InvoiceDTO result = invoiceService.createInvoice(mockDto);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(80), mockInvoice.getOutstanding());
        assertEquals(Invoice.Status.ISSUED, mockInvoice.getStatus());
        assertNotNull(mockInvoice.getInvoiceNumber());
        assertNotNull(mockInvoice.getMembership());
        assertEquals(20L, mockInvoice.getMembership().getMemId());
        verify(invoiceRepo).save(mockInvoice);
        verify(auditLogService).logForCurrentUser(eq("Invoice"), eq(10L), eq(AuditLog.Action.CREATE), isNull(), anyString());
    }

    @Test
    void createInvoice_MemberNotFound() {
        when(memberRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> invoiceService.createInvoice(mockDto));
    }

    @Test
    void voidInvoice_Success() {
        when(invoiceRepo.findById(10L)).thenReturn(Optional.of(mockInvoice));
        when(invoiceRepo.save(any(Invoice.class))).thenReturn(mockInvoice);
        when(mapper.map(any(Invoice.class), eq(InvoiceDTO.class))).thenReturn(mockDto);

        InvoiceDTO result = invoiceService.voidInvoice(10L, "Customer requested");

        assertNotNull(result);
        assertEquals(Invoice.Status.VOID, mockInvoice.getStatus());
        verify(auditLogService).logForCurrentUser(eq("Invoice"), eq(10L), eq(AuditLog.Action.UPDATE), isNull(), anyString());
    }

    @Test
    void voidInvoice_AlreadyPaid() {
        mockInvoice.setStatus(Invoice.Status.PAID);
        when(invoiceRepo.findById(10L)).thenReturn(Optional.of(mockInvoice));

        assertThrows(BusinessRuleException.class, () -> invoiceService.voidInvoice(10L, "Reason"));
    }

    @Test
    void getInvoiceEntityById_Success() {
        when(invoiceRepo.findById(10L)).thenReturn(Optional.of(mockInvoice));
        Invoice result = invoiceService.getInvoiceEntityById(10L);
        assertNotNull(result);
    }

    @Test
    void getInvoicesEntityByMember_Success() {
        when(invoiceRepo.findByMemberMemberId(1L)).thenReturn(Collections.singletonList(mockInvoice));
        List<Invoice> results = invoiceService.getInvoicesEntityByMember(1L);
        assertEquals(1, results.size());
    }

    @Test
    void getInvoicesByMember_Success() {
        when(invoiceRepo.findByMemberMemberId(1L)).thenReturn(Collections.singletonList(mockInvoice));
        when(mapper.map(any(Invoice.class), eq(InvoiceDTO.class))).thenReturn(mockDto);

        List<InvoiceDTO> results = invoiceService.getInvoicesByMember(1L);
        assertEquals(1, results.size());
    }

    @Test
    void getInvoiceById_Success() {
        when(invoiceRepo.findById(10L)).thenReturn(Optional.of(mockInvoice));
        when(mapper.map(any(Invoice.class), eq(InvoiceDTO.class))).thenReturn(mockDto);

        InvoiceDTO result = invoiceService.getInvoiceById(10L);
        assertNotNull(result);
    }
}
