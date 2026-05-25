package com.fitness.service;

import com.fitness.entity.Membership;
import com.fitness.entity.Plan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ProratedPriceServiceTest {

    @InjectMocks
    private ProratedPriceService proratedPriceService;

    @Test
    void calculateRemainingValue_Success() {
        Plan plan = new Plan();
        plan.setProrationRule("PRO-RATA");

        Membership membership = new Membership();
        membership.setPlan(plan);
        membership.setDuration(30);
        membership.setPrice(BigDecimal.valueOf(3000));
        membership.setEndDate(LocalDate.now().plusDays(15)); // 15 days remaining

        BigDecimal remainingValue = proratedPriceService.calculateRemainingValue(membership);
        
        // 3000 / 30 = 100 per day. 100 * 15 = 1500
        assertEquals(new BigDecimal("1500.00"), remainingValue);
    }

    @Test
    void calculateRemainingValue_Expired_ReturnsZero() {
        Membership membership = new Membership();
        membership.setEndDate(LocalDate.now().minusDays(1));
        
        BigDecimal remainingValue = proratedPriceService.calculateRemainingValue(membership);
        assertEquals(BigDecimal.ZERO, remainingValue);
    }

    @Test
    void calculateRemainingValue_NoneRule_ReturnsZero() {
        Plan plan = new Plan();
        plan.setProrationRule("NONE");

        Membership membership = new Membership();
        membership.setPlan(plan);
        membership.setEndDate(LocalDate.now().plusDays(15));
        
        BigDecimal remainingValue = proratedPriceService.calculateRemainingValue(membership);
        assertEquals(BigDecimal.ZERO, remainingValue);
    }

    @Test
    void calculateProratedPrice_Success() {
        BigDecimal prorated = proratedPriceService.calculateProratedPrice(BigDecimal.valueOf(3000), 10, 30);
        assertEquals(new BigDecimal("1000.00"), prorated);
    }

    @Test
    void isValidUpgrade_True() {
        Plan plan = new Plan();
        plan.setProrationRule("PRO-RATA");

        Membership membership = new Membership();
        membership.setPlan(plan);
        membership.setDuration(30);
        membership.setPrice(BigDecimal.valueOf(3000));
        membership.setEndDate(LocalDate.now().plusDays(15)); // remaining: 1500

        assertTrue(proratedPriceService.isValidUpgrade(membership, BigDecimal.valueOf(2000)));
    }

    @Test
    void isValidUpgrade_False() {
        Plan plan = new Plan();
        plan.setProrationRule("PRO-RATA");

        Membership membership = new Membership();
        membership.setPlan(plan);
        membership.setDuration(30);
        membership.setPrice(BigDecimal.valueOf(3000));
        membership.setEndDate(LocalDate.now().plusDays(15)); // remaining: 1500

        assertFalse(proratedPriceService.isValidUpgrade(membership, BigDecimal.valueOf(1000)));
    }
}
