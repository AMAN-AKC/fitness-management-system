package com.fitness.service;

import com.fitness.dto.PlanDTO;
import com.fitness.entity.Plan;
import com.fitness.entity.AuditLog.Action;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.MembershipRepository;
import com.fitness.repository.PlanRepository;
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

    @Test
    void createPlan_Success() {
        PlanDTO dto = new PlanDTO();
        dto.setPlanName("Basic Plan");
        Plan plan = new Plan();
        plan.setPlanName("Basic Plan");

        when(planRepo.existsByPlanName(dto.getPlanName())).thenReturn(false);
        when(mapper.map(dto, Plan.class)).thenReturn(plan);
        when(planRepo.save(any(Plan.class))).thenReturn(plan);
        when(mapper.map(plan, PlanDTO.class)).thenReturn(dto);

        PlanDTO result = planService.createPlan(dto);

        assertNotNull(result);
        assertEquals("Basic Plan", result.getPlanName());
        verify(planRepo).save(any(Plan.class));
        verify(auditLogService).logForCurrentUser(eq("Plan"), any(), eq(Action.CREATE), anyString(), isNull());
    }

    @Test
    void createPlan_DuplicateName_ThrowsException() {
        PlanDTO dto = new PlanDTO();
        dto.setPlanName("Basic Plan");

        when(planRepo.existsByPlanName(dto.getPlanName())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> planService.createPlan(dto));
        verify(planRepo, never()).save(any());
    }

    @Test
    void getPlanById_Found() {
        Plan plan = new Plan();
        plan.setPlanId(1L);
        PlanDTO dto = new PlanDTO();
        dto.setPlanId(1L);

        when(planRepo.findById(1L)).thenReturn(Optional.of(plan));
        when(mapper.map(plan, PlanDTO.class)).thenReturn(dto);

        PlanDTO result = planService.getPlanById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getPlanId());
    }

    @Test
    void getPlanById_NotFound_ThrowsException() {
        when(planRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> planService.getPlanById(1L));
    }

    @Test
    void getAllPlans_ReturnsList() {
        Plan plan = new Plan();
        PlanDTO dto = new PlanDTO();

        when(planRepo.findAll()).thenReturn(List.of(plan));
        when(mapper.map(plan, PlanDTO.class)).thenReturn(dto);

        List<PlanDTO> result = planService.getAllPlans();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getActivePlans_ReturnsList() {
        Plan plan = new Plan();
        PlanDTO dto = new PlanDTO();

        when(planRepo.findByIsActiveTrue()).thenReturn(List.of(plan));
        when(mapper.map(plan, PlanDTO.class)).thenReturn(dto);

        List<PlanDTO> result = planService.getActivePlans();
        assertFalse(result.isEmpty());
    }

    @Test
    void updatePlan_Success() {
        Plan plan = new Plan();
        plan.setPlanId(1L);
        plan.setPrice(BigDecimal.TEN);
        plan.setVersion(1);

        PlanDTO dto = new PlanDTO();
        
        when(planRepo.findById(1L)).thenReturn(Optional.of(plan));
        when(planRepo.save(any(Plan.class))).thenReturn(plan);
        lenient().when(mapper.map(any(), eq(PlanDTO.class))).thenReturn(dto);

        PlanDTO result = planService.updatePlan(1L, dto);

        assertNotNull(result);
        verify(mapper).map(dto, plan);
        verify(planRepo).save(plan);
        verify(auditLogService).logForCurrentUser(eq("Plan"), eq(1L), eq(Action.UPDATE), anyString(), anyString());
    }

    @Test
    void deactivatePlan_Success() {
        Plan plan = new Plan();
        plan.setPlanId(1L);
        plan.setIsActive(true);

        when(planRepo.findById(1L)).thenReturn(Optional.of(plan));
        when(membershipRepo.findByStatus(any())).thenReturn(List.of());

        planService.deactivatePlan(1L);

        assertFalse(plan.getIsActive());
        verify(planRepo).save(plan);
        verify(auditLogService).logForCurrentUser(eq("Plan"), eq(1L), eq(Action.UPDATE), anyString(), anyString());
    }
}
