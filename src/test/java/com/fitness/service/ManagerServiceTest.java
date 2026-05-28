package com.fitness.service;

import com.fitness.dto.ManagerDashboardDto;
import com.fitness.entity.*;
import com.fitness.enums.Role;
import com.fitness.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagerServiceTest {

    @InjectMocks
    private ManagerService managerService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private ClassesRepository classesRepository;
    @Mock
    private ClassBookingRepository bookingRepository;
    @Mock
    private SystemUserRepository userRepo;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    private SystemUser mockAdmin;
    private SystemUser mockManager;
    private Branch mockBranch;

    @BeforeEach
    void setUp() {
        mockBranch = new Branch();
        mockBranch.setBranchId(1L);
        mockBranch.setBranchName("HQ");

        mockAdmin = new SystemUser();
        mockAdmin.setUsername("admin");
        mockAdmin.setRole(Role.ADMIN);

        mockManager = new SystemUser();
        mockManager.setUsername("manager");
        mockManager.setRole(Role.MANAGER);
        mockManager.setBranch(mockBranch);
    }

    private void mockSecurityContext(SystemUser user) {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(user.getUsername());
        lenient().when(userRepo.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    }

    private void mockNoSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getDashboardStats_AsAdmin_AllBranches() {
        mockSecurityContext(mockAdmin);

        when(memberRepository.countByStatus(Member.Status.ACTIVE)).thenReturn(100L);
        when(memberRepository.countByCreatedAtBetween(any(), any())).thenReturn(10L); // called multiple times in loop, so we just mock generically
        
        when(invoiceRepository.sumRevenueByCreatedAtBetween(any(), any())).thenReturn(BigDecimal.valueOf(5000));
        
        when(classesRepository.count()).thenReturn(20L);
        when(memberRepository.countByStatus(Member.Status.DEACTIVATED)).thenReturn(5L);
        
        Classes c1 = new Classes();
        c1.setClassId(10L);
        c1.setClassName("Yoga");
        c1.setCapacity(20);
        when(classesRepository.findAll()).thenReturn(Collections.singletonList(c1));
        
        when(bookingRepository.countByFitnessClassClassIdAndBookingStatus(eq(10L), any())).thenReturn(10L);

        Invoice overdue = new Invoice();
        overdue.setInvoiceId(99L);
        Member m = new Member();
        m.setMemName("John");
        m.setEmail("john@example.com");
        overdue.setMember(m);
        overdue.setFinalAmount(BigDecimal.valueOf(100));
        overdue.setCreatedAt(LocalDateTime.now().minusDays(10));
        overdue.setStatus(Invoice.Status.OVERDUE);
        
        when(invoiceRepository.findByStatusIn(anyList())).thenReturn(Collections.singletonList(overdue));

        ManagerDashboardDto dto = managerService.getDashboardStats(null);

        assertNotNull(dto);
        assertEquals(100L, dto.getActiveMembers());
        assertEquals(10L, dto.getNewJoinsThisMonth());
        assertEquals(5000.0, dto.getMonthlyRevenue());
        assertEquals(20L, dto.getClassesThisWeek());
        assertEquals(50.0, dto.getAvgClassOccupancy());
        
        assertEquals(6, dto.getRevenueAnalytics().size()); // 6 months
        
        assertEquals(1, dto.getTopClasses().size());
        assertEquals("Yoga", dto.getTopClasses().get(0).getName());
        
        assertEquals(1, dto.getDunningQueue().size());
        assertEquals(99, dto.getDunningQueue().get(0).getInvoiceId());
    }

    @Test
    void getDashboardStats_AsManager_SpecificBranch() {
        mockSecurityContext(mockManager);

        when(memberRepository.countByStatusAndHomeBranchBranchId(Member.Status.ACTIVE, 1L)).thenReturn(50L);
        when(memberRepository.countByCreatedAtBetweenAndHomeBranchBranchId(any(), any(), eq(1L))).thenReturn(5L);
        
        when(invoiceRepository.sumRevenueByCreatedAtBetweenAndBranchId(any(), any(), eq(1L))).thenReturn(BigDecimal.valueOf(2500));
        
        when(classesRepository.countByBranchBranchId(1L)).thenReturn(10L);
        when(memberRepository.countByStatusAndHomeBranchBranchId(Member.Status.DEACTIVATED, 1L)).thenReturn(2L);
        
        when(classesRepository.findByBranchBranchId(1L)).thenReturn(Collections.emptyList());
        when(invoiceRepository.findByStatusInAndBranchId(anyList(), eq(1L))).thenReturn(Collections.emptyList());

        ManagerDashboardDto dto = managerService.getDashboardStats(null);

        assertNotNull(dto);
        assertEquals(50L, dto.getActiveMembers());
        assertEquals(2500.0, dto.getMonthlyRevenue());
    }

    @Test
    void getDashboardStats_WithBranchOverride() {
        mockNoSecurityContext(); // It shouldn't crash, should handle null currentUser gracefully if security context is null but branchId is provided

        when(memberRepository.countByStatusAndHomeBranchBranchId(Member.Status.ACTIVE, 2L)).thenReturn(10L);
        when(memberRepository.countByCreatedAtBetweenAndHomeBranchBranchId(any(), any(), eq(2L))).thenReturn(1L);
        
        when(invoiceRepository.sumRevenueByCreatedAtBetweenAndBranchId(any(), any(), eq(2L))).thenReturn(BigDecimal.valueOf(500));
        
        when(classesRepository.countByBranchBranchId(2L)).thenReturn(5L);
        when(memberRepository.countByStatusAndHomeBranchBranchId(Member.Status.DEACTIVATED, 2L)).thenReturn(0L);
        
        when(classesRepository.findByBranchBranchId(2L)).thenReturn(Collections.emptyList());
        when(invoiceRepository.findByStatusInAndBranchId(anyList(), eq(2L))).thenReturn(Collections.emptyList());

        ManagerDashboardDto dto = managerService.getDashboardStats(2L);

        assertNotNull(dto);
        assertEquals(10L, dto.getActiveMembers());
        assertEquals(500.0, dto.getMonthlyRevenue());
    }

    @Test
    void exportAnalyticsCsv_Success() {
        mockSecurityContext(mockAdmin);
        
        when(memberRepository.countByStatus(any())).thenReturn(0L);
        when(memberRepository.countByCreatedAtBetween(any(), any())).thenReturn(10L);
        when(invoiceRepository.sumRevenueByCreatedAtBetween(any(), any())).thenReturn(BigDecimal.valueOf(1000));
        when(classesRepository.findAll()).thenReturn(Collections.emptyList());
        when(invoiceRepository.findByStatusIn(anyList())).thenReturn(Collections.emptyList());

        String csv = managerService.exportAnalyticsCsv();

        assertNotNull(csv);
        assertTrue(csv.contains("Month,Revenue,NewJoins,Churn"));
        assertTrue(csv.contains("1000.0")); // Ensure data is serialized
    }
}
