package com.fitness.service;

import com.fitness.dto.PaymentDTO;
import com.fitness.dto.ReceiptDTO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.InvoiceRepository;
import com.fitness.repository.MemberRepository;
import com.fitness.repository.PaymentRepository;
import com.fitness.repository.SystemUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepo;
    @Mock
    private InvoiceRepository invoiceRepo;
    @Mock
    private MemberRepository memberRepo;
    @Mock
    private SystemUserRepository userRepo;
    @Mock
    private ModelMapper mapper;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private ReceiptService receiptService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private DunningService dunningService;
    @Mock
    private HealthConsentService healthConsentService;
    @Mock
    private PaymentGatewayProcessor paymentGatewayProcessor;

    private PaymentService paymentService;

    private Member mockMember;
    private Invoice mockInvoice;
    private Payment mockPayment;
    private PaymentDTO mockDto;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepo, invoiceRepo, memberRepo, userRepo, mapper,
                auditLogService, receiptService, eventPublisher,
                dunningService, healthConsentService, Collections.singletonList(paymentGatewayProcessor)
        );

        mockMember = new Member();
        mockMember.setMemberId(1L);
        mockMember.setWalletBalance(BigDecimal.valueOf(500));
        mockMember.setStatus(Member.Status.ACTIVE);

        mockInvoice = new Invoice();
        mockInvoice.setInvoiceId(10L);
        mockInvoice.setFinalAmount(BigDecimal.valueOf(100));
        mockInvoice.setPaidAmount(BigDecimal.ZERO);
        mockInvoice.setOutstanding(BigDecimal.valueOf(100));

        mockPayment = new Payment();
        mockPayment.setPaymentId(100L);
        mockPayment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);

        mockDto = new PaymentDTO();
        mockDto.setInvoiceId(10L);
        mockDto.setMemberId(1L);
        mockDto.setAmountPaid(BigDecimal.valueOf(100));
        mockDto.setPaymentMethod(Payment.PaymentMethod.CARD);
    }

    @Test
    void processPayment_Success() {
        when(invoiceRepo.findById(10L)).thenReturn(Optional.of(mockInvoice));
        when(memberRepo.findById(1L)).thenReturn(Optional.of(mockMember));
        when(paymentGatewayProcessor.supports(Payment.PaymentMethod.CARD)).thenReturn(true);
        when(paymentGatewayProcessor.process(any(), any(), any())).thenReturn(new PaymentProcessingResult(true, Payment.PaymentStatus.SUCCESS, null, null));
        when(mapper.map(any(PaymentDTO.class), eq(Payment.class))).thenReturn(mockPayment);
        when(paymentRepo.save(any(Payment.class))).thenReturn(mockPayment);
        ReceiptDTO mockReceipt = new ReceiptDTO();
        mockReceipt.setTotalAmount(BigDecimal.valueOf(100));
        mockReceipt.setMemberName("John Doe");
        mockReceipt.setReceiptNumber("REC-123");
        mockReceipt.setPlanName("Basic Plan");
        mockReceipt.setPaymentDate(LocalDateTime.now());
        when(receiptService.generateReceipt(10L, 100L)).thenReturn(mockReceipt);
        when(mapper.map(any(Payment.class), eq(PaymentDTO.class))).thenReturn(mockDto);

        PaymentDTO result = paymentService.processPayment(mockDto);

        assertNotNull(result);
        assertEquals(Invoice.Status.PAID, mockInvoice.getStatus());
        verify(paymentRepo).save(mockPayment);
        verify(invoiceRepo).save(mockInvoice);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void processPayment_WalletInsufficient() {
        mockDto.setWalletCreditApplied(BigDecimal.valueOf(600)); // More than wallet balance
        when(invoiceRepo.findById(10L)).thenReturn(Optional.of(mockInvoice));
        when(memberRepo.findById(1L)).thenReturn(Optional.of(mockMember));
        when(paymentGatewayProcessor.supports(Payment.PaymentMethod.CARD)).thenReturn(true);
        when(paymentGatewayProcessor.process(any(), any(), any())).thenReturn(new PaymentProcessingResult(true, Payment.PaymentStatus.SUCCESS, null, null));

        assertThrows(BusinessRuleException.class, () -> paymentService.processPayment(mockDto));
    }

    @Test
    void processPayment_PaymentFailed() {
        when(invoiceRepo.findById(10L)).thenReturn(Optional.of(mockInvoice));
        when(memberRepo.findById(1L)).thenReturn(Optional.of(mockMember));
        when(paymentGatewayProcessor.supports(Payment.PaymentMethod.CARD)).thenReturn(true);
        when(paymentGatewayProcessor.process(any(), any(), any())).thenReturn(new PaymentProcessingResult(false, Payment.PaymentStatus.FAILED, "Declined", null));
        when(mapper.map(any(PaymentDTO.class), eq(Payment.class))).thenReturn(mockPayment);
        when(paymentRepo.save(any(Payment.class))).thenReturn(mockPayment);
        when(mapper.map(any(Payment.class), eq(PaymentDTO.class))).thenReturn(mockDto);

        PaymentDTO result = paymentService.processPayment(mockDto);

        assertNotNull(result);
        assertEquals(Invoice.Status.OVERDUE, mockInvoice.getStatus());
        verify(dunningService).handleFailedPayment(10L, "Declined");
    }

    @Test
    void refundPayment_Success() {
        mockPayment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);
        when(paymentRepo.findById(100L)).thenReturn(Optional.of(mockPayment));
        SystemUser refundUser = new SystemUser();
        refundUser.setUsername("admin");
        when(userRepo.findById(2L)).thenReturn(Optional.of(refundUser));
        when(paymentRepo.save(any(Payment.class))).thenReturn(mockPayment);
        when(mapper.map(any(Payment.class), eq(PaymentDTO.class))).thenReturn(mockDto);

        PaymentDTO result = paymentService.refundPayment(100L, 2L, "Requested");
        
        assertNotNull(result);
        assertEquals(Payment.PaymentStatus.REFUNDED, mockPayment.getPaymentStatus());
    }

    @Test
    void getPaymentsByMember_Success() {
        when(paymentRepo.findByMemberMemberId(1L)).thenReturn(Collections.singletonList(mockPayment));
        List<PaymentDTO> results = paymentService.getPaymentsByMember(1L);
        assertEquals(1, results.size());
    }

    @Test
    void getFailedPayments_Success() {
        when(paymentRepo.findByPaymentStatus(Payment.PaymentStatus.FAILED)).thenReturn(Collections.singletonList(mockPayment));
        List<PaymentDTO> results = paymentService.getFailedPayments();
        assertEquals(1, results.size());
    }

    @Test
    void getRevenueMTD_Success() {
        when(paymentRepo.sumRevenueSince(any(LocalDateTime.class), eq(Payment.PaymentStatus.SUCCESS))).thenReturn(BigDecimal.valueOf(1000));
        BigDecimal result = paymentService.getRevenueMTD();
        assertEquals(BigDecimal.valueOf(1000), result);
    }
}
