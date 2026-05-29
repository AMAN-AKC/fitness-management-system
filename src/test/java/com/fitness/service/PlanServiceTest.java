package com.fitness.service;

import com.fitness.dto.PlanDTO;
import com.fitness.entity.AuditLog;
import com.fitness.entity.Membership;
import com.fitness.entity.Plan;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.MembershipRepository;
import com.fitness.repository.PlanRepository;
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
public class PlanServiceTest {

    @InjectMocks
    private PlanService planService;

    @Mock
    private PlanRepository planRepo;
    @Mock
    private MembershipRepository membershipRepo;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private ModelMapper mapper;

    private Plan mockPlan;
    private PlanDTO mockDto;

    @BeforeEach
    void setUp() {
        mockPlan = new Plan();
        mockPlan.setPlanId(1L);
        mockPlan.setPlanName("Gold Plan");
        mockPlan.setPrice(BigDecimal.valueOf(100));
        mockPlan.setTaxPercent(BigDecimal.valueOf(10));
        mockPlan.setVersion(1);
        mockPlan.setIsActive(true);

        mockDto = new PlanDTO();
        mockDto.setPlanId(1L);
        mockDto.setPlanName("Gold Plan");
    }

    @Test
    void createPlan_Success() {
        when(planRepo.existsByPlanName("Gold Plan")).thenReturn(false);
        when(mapper.map(any(PlanDTO.class), eq(Plan.class))).thenReturn(mockPlan);
        when(planRepo.save(any(Plan.class))).thenReturn(mockPlan);
        when(mapper.map(any(Plan.class), eq(PlanDTO.class))).thenReturn(mockDto);

        PlanDTO result = planService.createPlan(mockDto);

        assertNotNull(result);
        assertEquals(1, mockPlan.getVersion());
        assertTrue(mockPlan.getIsActive());
        verify(planRepo).save(mockPlan);
        verify(auditLogService).logForCurrentUser(eq("Plan"), eq(1L), eq(AuditLog.Action.CREATE), anyString(),
                isNull());
    }

    @Test
    void createPlan_Duplicate_ThrowsException() {
        when(planRepo.existsByPlanName("Gold Plan")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> planService.createPlan(mockDto));
    }

    @Test
    void getAllPlans_Success() {
        when(planRepo.findAll()).thenReturn(Collections.singletonList(mockPlan));
        when(mapper.map(any(Plan.class), eq(PlanDTO.class))).thenReturn(mockDto);

        List<PlanDTO> results = planService.getAllPlans();
        assertEquals(1, results.size());
    }

    @Test
    void getActivePlans_Success() {
        when(planRepo.findByIsActiveTrue()).thenReturn(Collections.singletonList(mockPlan));
        when(mapper.map(any(Plan.class), eq(PlanDTO.class))).thenReturn(mockDto);

        List<PlanDTO> results = planService.getActivePlans();
        assertEquals(1, results.size());
    }

    @Test
    void getPlanById_Success() {
        when(planRepo.findById(1L)).thenReturn(Optional.of(mockPlan));
        when(mapper.map(any(Plan.class), eq(PlanDTO.class))).thenReturn(mockDto);

        PlanDTO result = planService.getPlanById(1L);
        assertNotNull(result);
    }

    @Test
    void getPlanById_NotFound() {
        when(planRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> planService.getPlanById(1L));
    }

    @Test
    void updatePlan_Success() {
        when(planRepo.findById(1L)).thenReturn(Optional.of(mockPlan));
        when(planRepo.save(any(Plan.class))).thenReturn(mockPlan);
        doNothing().when(mapper).map(any(PlanDTO.class), any(Plan.class));
        when(mapper.map(any(Plan.class), eq(PlanDTO.class))).thenReturn(mockDto);

        PlanDTO result = planService.updatePlan(1L, mockDto);

        assertNotNull(result);
        assertEquals(2, mockPlan.getVersion());
        verify(mapper).map(mockDto, mockPlan);
        verify(auditLogService).logForCurrentUser(eq("Plan"), eq(1L), eq(AuditLog.Action.UPDATE), anyString(),
                anyString());
    }

    @Test
    void deletePlan_WhenInUse_ThrowsException() {
        when(planRepo.findById(1L)).thenReturn(Optional.of(mockPlan));

        Membership m = new Membership();
        m.setPlan(mockPlan);
        when(membershipRepo.findByStatus(Membership.Status.ACTIVE)).thenReturn(Collections.singletonList(m));

        assertThrows(BusinessRuleException.class, () -> planService.deletePlan(1L));

        verify(planRepo, never()).delete(any());
    }

    @Test
    void deletePlan_Success() {
        when(planRepo.findById(1L)).thenReturn(Optional.of(mockPlan));
        when(membershipRepo.findByStatus(Membership.Status.ACTIVE)).thenReturn(Collections.emptyList());

        planService.deletePlan(1L);

        verify(planRepo).delete(mockPlan);
        verify(auditLogService).logForCurrentUser(eq("Plan"), eq(1L), eq(AuditLog.Action.DELETE), anyString(),
                eq("DELETED"));
    }
}
