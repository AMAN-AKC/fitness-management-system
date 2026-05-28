package com.fitness.service;

import com.fitness.dto.PriceBreakdownDTO;
import com.fitness.entity.Membership;
import com.fitness.entity.Plan;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.MembershipRepository;
import com.fitness.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PriceBreakdownServiceTest {

    @InjectMocks
    private PriceBreakdownService priceBreakdownService;

    @Mock
    private PlanRepository planRepo;
    @Mock
    private MembershipRepository membershipRepo;
    @Mock
    private ProratedPriceService proratedPriceService;

    private Plan mockPlan;
    private Plan mockNewPlan;
    private Membership mockMembership;

    @BeforeEach
    void setUp() {
        mockPlan = new Plan();
        mockPlan.setPlanId(1L);
        mockPlan.setPlanName("Basic");
        mockPlan.setPrice(BigDecimal.valueOf(100));
        mockPlan.setTaxPercent(BigDecimal.valueOf(10));
        mockPlan.setDurationDays(30);

        mockNewPlan = new Plan();
        mockNewPlan.setPlanId(2L);
        mockNewPlan.setPlanName("Premium");
        mockNewPlan.setPrice(BigDecimal.valueOf(200));
        mockNewPlan.setTaxPercent(BigDecimal.valueOf(10));
        mockNewPlan.setDurationDays(30);

        mockMembership = new Membership();
        mockMembership.setMemId(10L);
        mockMembership.setPlan(mockPlan);
        mockMembership.setEndDate(LocalDate.now().plusDays(15));
        mockMembership.setStatus(Membership.Status.ACTIVE);
    }

    @Test
    void calculateNewPlanBreakdown_Success() {
        when(planRepo.findById(1L)).thenReturn(Optional.of(mockPlan));

        PriceBreakdownDTO result = priceBreakdownService.calculateNewPlanBreakdown(1L, BigDecimal.valueOf(10));

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100), result.getBasePrice());
        assertEquals(BigDecimal.valueOf(10), result.getDiscount());
        assertEquals(BigDecimal.valueOf(90), result.getPriceAfterDiscount());
        assertEquals(new BigDecimal("9.00"), result.getTaxAmount()); // 10% of 90
        assertEquals(new BigDecimal("99.00"), result.getFinalAmount()); // 90 + 9
        assertEquals("NEW_PLAN", result.getType());
    }

    @Test
    void calculateNewPlanBreakdown_PlanNotFound() {
        when(planRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> priceBreakdownService.calculateNewPlanBreakdown(1L, BigDecimal.ZERO));
    }

    @Test
    void calculateUpgradeBreakdown_Success() {
        when(membershipRepo.findByMemberMemberIdAndStatus(100L, Membership.Status.ACTIVE))
                .thenReturn(Collections.singletonList(mockMembership));
        when(planRepo.findById(2L)).thenReturn(Optional.of(mockNewPlan));
        when(proratedPriceService.calculateRemainingValue(any(Membership.class)))
                .thenReturn(BigDecimal.valueOf(50)); // $50 remaining value on old plan

        PriceBreakdownDTO result = priceBreakdownService.calculateUpgradeBreakdown(100L, 2L, BigDecimal.valueOf(10));

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(200), result.getBasePrice());
        assertEquals(BigDecimal.valueOf(10), result.getDiscount());
        // Price = 200 - 50 (remaining) - 10 (discount) = 140
        assertEquals(BigDecimal.valueOf(140), result.getPriceAfterDiscount());
        assertEquals(new BigDecimal("14.00"), result.getTaxAmount()); // 10% of 140
        assertEquals(new BigDecimal("154.00"), result.getFinalAmount());
        assertEquals("UPGRADE", result.getType());
        assertNotNull(result.getProration());
        assertEquals(BigDecimal.valueOf(50), result.getProration().getRemainingValue());
    }

    @Test
    void calculateUpgradeBreakdown_NoActiveMembership() {
        when(membershipRepo.findByMemberMemberIdAndStatus(100L, Membership.Status.ACTIVE))
                .thenReturn(Collections.emptyList());
        
        assertThrows(ResourceNotFoundException.class, () -> priceBreakdownService.calculateUpgradeBreakdown(100L, 2L, BigDecimal.ZERO));
    }

    @Test
    void calculateUpgradeBreakdown_NewPlanNotFound() {
        when(membershipRepo.findByMemberMemberIdAndStatus(100L, Membership.Status.ACTIVE))
                .thenReturn(Collections.singletonList(mockMembership));
        when(planRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> priceBreakdownService.calculateUpgradeBreakdown(100L, 2L, BigDecimal.ZERO));
    }
}
