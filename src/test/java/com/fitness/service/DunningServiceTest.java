package com.fitness.service;

import com.fitness.entity.Invoice;
import com.fitness.entity.Member;
import com.fitness.entity.Membership;
import com.fitness.repository.InvoiceRepository;
import com.fitness.repository.MemberRepository;
import com.fitness.repository.MembershipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DunningServiceTest {

    @InjectMocks
    private DunningService dunningService;

    @Mock
    private InvoiceRepository invoiceRepo;

    @Mock
    private MembershipRepository membershipRepo;

    @Mock
    private MemberRepository memberRepo;

    @Mock
    private AuditLogService auditLogService;

    @Test
    void handleFailedPayment_Success() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(1L);

        Membership membership = new Membership();
        membership.setMemId(1L);
        invoice.setMembership(membership);

        when(invoiceRepo.findById(1L)).thenReturn(Optional.of(invoice));

        dunningService.handleFailedPayment(1L, "Insufficient Funds");

        assertEquals(Invoice.Status.OVERDUE, invoice.getStatus());
        assertEquals(Membership.Status.PENDING, membership.getStatus());
        verify(invoiceRepo).save(invoice);
        verify(membershipRepo).save(membership);
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
    }

    @Test
    void transitionToDunning_Success() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(1L);
        invoice.setStatus(Invoice.Status.OVERDUE);

        Membership membership = new Membership();
        membership.setMemId(1L);
        invoice.setMembership(membership);

        when(invoiceRepo.findById(1L)).thenReturn(Optional.of(invoice));

        dunningService.transitionToDunning(1L);

        assertEquals(Membership.Status.DUNNING, membership.getStatus());
        verify(membershipRepo).save(membership);
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
    }

    @Test
    void resolveDunning_Success() {
        Membership membership = new Membership();
        membership.setMemId(1L);
        membership.setStatus(Membership.Status.DUNNING);

        when(membershipRepo.findById(1L)).thenReturn(Optional.of(membership));

        dunningService.resolveDunning(1L);

        assertEquals(Membership.Status.ACTIVE, membership.getStatus());
        verify(membershipRepo).save(membership);
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
    }

    @Test
    void recordFollowUp_Success() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(1L);

        when(invoiceRepo.findById(1L)).thenReturn(Optional.of(invoice));

        dunningService.recordFollowUp(1L, "Called customer");

        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
    }

    @Test
    void setPromiseToPay_Success() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(1L);

        when(invoiceRepo.findById(1L)).thenReturn(Optional.of(invoice));

        dunningService.setPromiseToPay(1L, LocalDate.now());

        assertNotNull(invoice.getPromiseToPayDate());
        verify(invoiceRepo).save(invoice);
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
    }
}
