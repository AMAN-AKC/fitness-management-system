package com.fitness.service;

import com.fitness.dto.PaymentDTO;
import com.fitness.entity.Invoice;
import com.fitness.entity.Member;
import com.fitness.entity.Payment;
import com.fitness.entity.SystemUser;
import com.fitness.exception.BusinessRuleException;
import com.fitness.repository.InvoiceRepository;
import com.fitness.repository.MemberRepository;
import com.fitness.repository.PaymentRepository;
import com.fitness.repository.SystemUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

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
    private EmailService emailService;

    @Mock
    private DunningService dunningService;

    @Mock
    private List<PaymentGatewayProcessor> paymentGatewayProcessors;

    @Test
    void processPayment_UnsupportedMethod_ThrowsException() {
        PaymentDTO dto = new PaymentDTO();
        dto.setInvoiceId(1L);
        dto.setMemberId(1L);
        dto.setPaymentMethod(Payment.PaymentMethod.CARD);

        Invoice invoice = new Invoice();
        Member member = new Member();

        when(invoiceRepo.findById(1L)).thenReturn(Optional.of(invoice));
        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(paymentGatewayProcessors.stream()).thenReturn(java.util.stream.Stream.empty());

        assertThrows(BusinessRuleException.class, () -> paymentService.processPayment(dto));
    }

    @Test
    void refundPayment_Success() {
        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);

        SystemUser user = new SystemUser();
        user.setUserId(1L);

        when(paymentRepo.findById(1L)).thenReturn(Optional.of(payment));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(paymentRepo.save(any(Payment.class))).thenReturn(payment);
        when(mapper.map(any(), eq(PaymentDTO.class))).thenReturn(new PaymentDTO());

        PaymentDTO result = paymentService.refundPayment(1L, 1L, "Requested");

        assertNotNull(result);
        assertEquals(Payment.PaymentStatus.REFUNDED, payment.getPaymentStatus());
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
    }

    @Test
    void getRevenueMTD_Success() {
        when(paymentRepo.sumRevenueSince(any())).thenReturn(BigDecimal.TEN);

        BigDecimal result = paymentService.getRevenueMTD();

        assertEquals(BigDecimal.TEN, result);
    }
}
