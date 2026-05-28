package com.fitness.service;

import com.fitness.dto.ReceiptDTO;
import com.fitness.entity.AuditLog;
import com.fitness.entity.Invoice;
import com.fitness.entity.Member;
import com.fitness.entity.Payment;
import com.fitness.entity.Receipt;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.InvoiceRepository;
import com.fitness.repository.PaymentRepository;
import com.fitness.repository.ReceiptRepository;
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
public class ReceiptServiceTest {

    @InjectMocks
    private ReceiptService receiptService;

    @Mock
    private ReceiptRepository receiptRepo;
    @Mock
    private InvoiceRepository invoiceRepo;
    @Mock
    private PaymentRepository paymentRepo;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private ModelMapper mapper;

    private Invoice mockInvoice;
    private Payment mockPayment;
    private Receipt mockReceipt;
    private ReceiptDTO mockDto;
    private Member mockMember;

    @BeforeEach
    void setUp() {
        mockMember = new Member();
        mockMember.setMemberId(1L);
        mockMember.setEmail("john@example.com");

        mockInvoice = new Invoice();
        mockInvoice.setInvoiceId(10L);
        mockInvoice.setTaxes(BigDecimal.valueOf(10));
        mockInvoice.setFinalAmount(BigDecimal.valueOf(110));
        mockInvoice.setMember(mockMember);

        mockPayment = new Payment();
        mockPayment.setPaymentId(100L);
        mockPayment.setAmountPaid(BigDecimal.valueOf(110));
        mockPayment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);

        mockReceipt = new Receipt();
        mockReceipt.setReceiptId(1000L);
        mockReceipt.setReceiptNumber("RCP-1234");
        mockReceipt.setMember(mockMember);

        mockDto = new ReceiptDTO();
        mockDto.setReceiptId(1000L);
        mockDto.setReceiptNumber("RCP-1234");
    }

    @Test
    void generateReceipt_Success() {
        when(invoiceRepo.findById(10L)).thenReturn(Optional.of(mockInvoice));
        when(paymentRepo.findById(100L)).thenReturn(Optional.of(mockPayment));
        when(receiptRepo.save(any(Receipt.class))).thenReturn(mockReceipt);
        when(mapper.map(any(Receipt.class), eq(ReceiptDTO.class))).thenReturn(mockDto);

        ReceiptDTO result = receiptService.generateReceipt(10L, 100L);

        assertNotNull(result);
        verify(receiptRepo).save(any(Receipt.class));
        verify(auditLogService).logForCurrentUser(eq("Receipt"), any(), eq(AuditLog.Action.CREATE), isNull(), anyString());
    }

    @Test
    void generateReceipt_PaymentNotSuccess() {
        mockPayment.setPaymentStatus(Payment.PaymentStatus.FAILED);
        when(invoiceRepo.findById(10L)).thenReturn(Optional.of(mockInvoice));
        when(paymentRepo.findById(100L)).thenReturn(Optional.of(mockPayment));

        assertThrows(BusinessRuleException.class, () -> receiptService.generateReceipt(10L, 100L));
    }

    @Test
    void markAsEmailed_Success() {
        when(receiptRepo.findById(1000L)).thenReturn(Optional.of(mockReceipt));
        when(receiptRepo.save(any(Receipt.class))).thenReturn(mockReceipt);
        when(mapper.map(any(Receipt.class), eq(ReceiptDTO.class))).thenReturn(mockDto);

        ReceiptDTO result = receiptService.markAsEmailed(1000L);

        assertNotNull(result);
        assertEquals(Receipt.Status.EMAILED, mockReceipt.getStatus());
        verify(auditLogService).logForCurrentUser(eq("Receipt"), eq(1000L), eq(AuditLog.Action.UPDATE), isNull(), anyString());
    }

    @Test
    void getReceiptsByMember_Success() {
        when(receiptRepo.findByMemberMemberId(1L)).thenReturn(Collections.singletonList(mockReceipt));
        when(mapper.map(any(Receipt.class), eq(ReceiptDTO.class))).thenReturn(mockDto);

        List<ReceiptDTO> results = receiptService.getReceiptsByMember(1L);
        assertEquals(1, results.size());
    }

    @Test
    void getReceiptByNumber_Success() {
        when(receiptRepo.findByReceiptNumber("RCP-1234")).thenReturn(Optional.of(mockReceipt));
        when(mapper.map(any(Receipt.class), eq(ReceiptDTO.class))).thenReturn(mockDto);

        ReceiptDTO result = receiptService.getReceiptByNumber("RCP-1234");
        assertNotNull(result);
    }

    @Test
    void getReceiptByNumber_NotFound() {
        when(receiptRepo.findByReceiptNumber("INVALID")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> receiptService.getReceiptByNumber("INVALID"));
    }
}
