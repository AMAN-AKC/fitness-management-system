package com.fitness.service;

import com.fitness.dto.ManagerDashboardDto;
import com.fitness.entity.Invoice;
import com.fitness.entity.Member;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class ManagerService {

    private final MemberRepository memberRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClassesRepository classesRepository;
    private final SystemUserRepository userRepo;

    private com.fitness.entity.SystemUser getCurrentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username).orElse(null);
    }

    public ManagerDashboardDto getDashboardStats() {
        log.info("Generating manager dashboard stats...");
        ManagerDashboardDto dto = new ManagerDashboardDto();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        log.info("Querying for month starting at: {} until {}", startOfMonth, now);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);

        com.fitness.entity.SystemUser currentUser = getCurrentUser();
        Long branchId = (currentUser != null && currentUser.getRole() != com.fitness.enums.Role.ADMIN && currentUser.getBranch() != null) 
                        ? currentUser.getBranch().getBranchId() : null;

        // 1. KPIs
        long activeCount = (branchId != null) 
            ? memberRepository.countByStatusAndHomeBranchBranchId(Member.Status.ACTIVE, branchId)
            : memberRepository.countByStatus(Member.Status.ACTIVE);
        log.info("Active members count: {}", activeCount);
        dto.setActiveMembers(activeCount);
        
        long newJoinsThisMonth = (branchId != null)
            ? memberRepository.countByCreatedAtBetweenAndHomeBranchBranchId(startOfMonth, now, branchId)
            : memberRepository.countByCreatedAtBetween(startOfMonth, now);
        log.info("New joins this month: {}", newJoinsThisMonth);
        
        long newJoinsLastMonth = (branchId != null)
            ? memberRepository.countByCreatedAtBetweenAndHomeBranchBranchId(startOfLastMonth, startOfMonth, branchId)
            : memberRepository.countByCreatedAtBetween(startOfLastMonth, startOfMonth);
        dto.setNewJoinsThisMonth(newJoinsThisMonth);
        dto.setNewJoinsTrend(calculateTrend(newJoinsThisMonth, newJoinsLastMonth));

        java.math.BigDecimal revenue = (branchId != null)
            ? invoiceRepository.sumRevenueByCreatedAtBetweenAndBranchId(startOfMonth, now, branchId)
            : invoiceRepository.sumRevenueByCreatedAtBetween(startOfMonth, now);
        log.info("Monthly revenue: {}", revenue);
        dto.setMonthlyRevenue(revenue != null ? revenue.doubleValue() : 0.0);

        long classCount = (branchId != null)
            ? classesRepository.countByBranchBranchId(branchId)
            : classesRepository.count();
        dto.setClassesThisWeek(classCount);
        dto.setAvgClassOccupancy(0.0); // Reset dummy occupancy

        // 2. Revenue Analytics (Last 6 Months)
        List<ManagerDashboardDto.RevenuePoint> analytics = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime start = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0);
            LocalDateTime end = start.plusMonths(1);
            
            ManagerDashboardDto.RevenuePoint point = new ManagerDashboardDto.RevenuePoint();
            point.setMonth(start.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            
            java.math.BigDecimal monthRev = (branchId != null)
                ? invoiceRepository.sumRevenueByCreatedAtBetweenAndBranchId(start, end, branchId)
                : invoiceRepository.sumRevenueByCreatedAtBetween(start, end);
            point.setRevenue(monthRev != null ? monthRev.doubleValue() : 0.0);
            point.setNewJoins((branchId != null)
                ? memberRepository.countByCreatedAtBetweenAndHomeBranchBranchId(start, end, branchId)
                : memberRepository.countByCreatedAtBetween(start, end));
            analytics.add(point);
        }
        dto.setRevenueAnalytics(analytics);

        // 3. Top Classes
        List<com.fitness.entity.Classes> classesList = (branchId != null)
            ? classesRepository.findByBranchBranchId(branchId)
            : classesRepository.findAll();

        dto.setTopClasses(classesList.stream().limit(5).map(c -> {
            ManagerDashboardDto.ClassUtilizationDto util = new ManagerDashboardDto.ClassUtilizationDto();
            util.setClassId(c.getClassId().intValue());
            util.setName(c.getClassName());
            util.setOccupancy(0.0); // Removed random data
            util.setFill("#2563EB");
            return util;
        }).collect(Collectors.toList()));

        // 4. Dunning Queue
        List<Invoice> overdueInvoices = (branchId != null)
            ? invoiceRepository.findByStatusInAndBranchId(List.of(Invoice.Status.OVERDUE, Invoice.Status.PENDING, Invoice.Status.UNPAID), branchId)
            : invoiceRepository.findByStatusIn(List.of(Invoice.Status.OVERDUE, Invoice.Status.PENDING, Invoice.Status.UNPAID));
        log.info("Found {} potential dunning items", overdueInvoices.size());
        dto.setDunningQueue(overdueInvoices.stream().limit(4).map(inv -> {
                ManagerDashboardDto.DunningMemberDto d = new ManagerDashboardDto.DunningMemberDto();
                d.setInvoiceId(inv.getInvoiceId().intValue());
                d.setName(inv.getMember().getMemName());
                d.setEmail(inv.getMember().getEmail());
                d.setOutstandingAmount(inv.getFinalAmount() != null ? inv.getFinalAmount().doubleValue() : 0.0);
                long daysSinceCreated = java.time.temporal.ChronoUnit.DAYS.between(inv.getCreatedAt(), LocalDateTime.now());
                d.setDaysOverdue((int) daysSinceCreated);
                d.setRetryDate("Not Scheduled");
                d.setStatus(inv.getStatus().name().toLowerCase());
                return d;
            }).collect(Collectors.toList()));

        return dto;
    }

    private double calculateTrend(long current, long previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return ((double) (current - previous) / previous) * 100.0;
    }
}
