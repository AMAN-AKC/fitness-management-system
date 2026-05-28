package com.fitness.service;

import com.fitness.dto.InvoiceDTO;
import com.fitness.dto.RecurringBillingScheduleDTO;
import com.fitness.entity.Invoice;
import com.fitness.entity.Member;
import com.fitness.entity.Membership;
import com.fitness.entity.Plan;
import com.fitness.repository.InvoiceRepository;
import com.fitness.repository.MembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecurringBillingServiceTest {

    @InjectMocks
    private RecurringBillingService recurringBillingService;

    @Mock
    private MembershipRepository membershipRepo;
    @Mock
    private InvoiceRepository invoiceRepo;
    @Mock
    private InvoiceService invoiceService;
    @Mock
    private EmailService emailService;
    @Mock
    private AuditLogService auditLogService;

    private Membership mockMembership;
    private Plan mockPlan;
    private Member mockMember;

    @BeforeEach
    void setUp() {
        mockPlan = new Plan();
        mockPlan.setPlanId(1L);
        mockPlan.setDurationDays(30);
        mockPlan.setPrice(BigDecimal.valueOf(50));
        mockPlan.setPlanName("Monthly Gold");

        mockMember = new Member();
        mockMember.setMemberId(10L);
        mockMember.setMemName("John Doe");

        mockMembership = new Membership();
        mockMembership.setMemId(100L);
        mockMembership.setPlan(mockPlan);
        mockMembership.setMember(mockMember);
        mockMembership.setStatus(Membership.Status.ACTIVE);
        mockMembership.setEndDate(LocalDate.now());
        mockMembership.setDiscountAmount(BigDecimal.valueOf(5));
    }

    @Test
    void isEligibleForRecurringBilling_True() {
        assertTrue(recurringBillingService.isEligibleForRecurringBilling(mockPlan));
    }

    @Test
    void isEligibleForRecurringBilling_False() {
        mockPlan.setDurationDays(15);
        assertFalse(recurringBillingService.isEligibleForRecurringBilling(mockPlan));
    }

    @Test
    void createRecurringSchedule_Eligible() {
        RecurringBillingScheduleDTO dto = recurringBillingService.createRecurringSchedule(mockMembership);

        assertNotNull(dto);
        assertTrue(dto.isRecurring());
        assertEquals(100L, dto.getMembershipId());
        assertEquals(10L, dto.getMemberId());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals("MONTHLY", dto.getFrequency());
        assertEquals(mockPlan.getPrice(), dto.getAmount());
    }

    @Test
    void createRecurringSchedule_NotEligible() {
        mockPlan.setDurationDays(15);
        RecurringBillingScheduleDTO dto = recurringBillingService.createRecurringSchedule(mockMembership);

        assertNotNull(dto);
        assertFalse(dto.isRecurring());
        assertEquals("NOT_ELIGIBLE", dto.getStatus());
    }

    @Test
    void getMembershipById_Success() {
        when(membershipRepo.findById(100L)).thenReturn(Optional.of(mockMembership));
        Membership m = recurringBillingService.getMembershipById(100L);
        assertNotNull(m);
        assertEquals(100L, m.getMemId());
    }

    @Test
    void getMembershipsForRecurringBilling_ReturnsActiveAndDue() {
        mockMembership.setEndDate(LocalDate.now().minusDays(1)); // Overdue is eligible
        
        Membership futureMembership = new Membership();
        futureMembership.setMemId(101L);
        futureMembership.setPlan(mockPlan);
        futureMembership.setEndDate(LocalDate.now().plusDays(10));
        futureMembership.setStatus(Membership.Status.ACTIVE);
        
        when(membershipRepo.findByStatus(Membership.Status.ACTIVE)).thenReturn(Arrays.asList(mockMembership, futureMembership));

        List<Membership> result = recurringBillingService.getMembershipsForRecurringBilling(LocalDate.now());
        
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getMemId());
    }

    @Test
    void processRecurringBilling_Success() {
        when(membershipRepo.findByStatus(Membership.Status.ACTIVE)).thenReturn(Collections.singletonList(mockMembership));
        when(invoiceRepo.existsByMembershipMemIdAndStatusIn(eq(100L), anyList())).thenReturn(false);
        
        InvoiceDTO createdInvoice = new InvoiceDTO();
        createdInvoice.setInvoiceNumber("INV-123");
        createdInvoice.setFinalAmount(BigDecimal.valueOf(45));
        when(invoiceService.createInvoice(any(InvoiceDTO.class))).thenReturn(createdInvoice);
        
        List<InvoiceDTO> results = recurringBillingService.processRecurringBilling(LocalDate.now());

        assertEquals(1, results.size());
        verify(invoiceService).createInvoice(any(InvoiceDTO.class));
        verify(emailService).sendRenewalReminder(eq(mockMember), eq("Monthly Gold"), anyString());
        verify(auditLogService).logForCurrentUser(eq("RecurringBilling"), eq(100L), any(), any(), anyString());
    }

    @Test
    void processRecurringBilling_SkipsIfOpenInvoiceExists() {
        when(membershipRepo.findByStatus(Membership.Status.ACTIVE)).thenReturn(Collections.singletonList(mockMembership));
        when(invoiceRepo.existsByMembershipMemIdAndStatusIn(eq(100L), anyList())).thenReturn(true);
        
        List<InvoiceDTO> results = recurringBillingService.processRecurringBilling(LocalDate.now());

        assertTrue(results.isEmpty());
        verify(invoiceService, never()).createInvoice(any());
    }

    @Test
    void runRecurringBillingJob_InvokesProcess() {
        when(membershipRepo.findByStatus(Membership.Status.ACTIVE)).thenReturn(Collections.emptyList());
        recurringBillingService.runRecurringBillingJob();
        verify(membershipRepo).findByStatus(Membership.Status.ACTIVE);
    }

    @Test
    void pauseRecurringBilling_Stubs() {
        assertDoesNotThrow(() -> recurringBillingService.pauseRecurringBilling(1L, "reason"));
    }

    @Test
    void resumeRecurringBilling_Stubs() {
        assertDoesNotThrow(() -> recurringBillingService.resumeRecurringBilling(1L));
    }
}
