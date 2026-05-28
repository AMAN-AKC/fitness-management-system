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
import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class ManagerService {

    private final MemberRepository memberRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClassesRepository classesRepository;
    private final ClassBookingRepository bookingRepository;
    private final SystemUserRepository userRepo;

    private com.fitness.entity.SystemUser getCurrentUser() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return userRepo.findByUsername(auth.getName()).orElse(null);
    }

    @Cacheable(value = "managerDashboard", key = "#branchIdOverride == null ? 'all' : #branchIdOverride")
    public ManagerDashboardDto getDashboardStats(Long branchIdOverride) {
        log.info("Generating manager dashboard stats...");
        ManagerDashboardDto dto = new ManagerDashboardDto();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        log.info("Querying for month starting at: {} until {}", startOfMonth, now);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);

        com.fitness.entity.SystemUser currentUser = getCurrentUser();
        Long branchId = branchIdOverride;
        if (branchId == null && currentUser != null && currentUser.getRole() != com.fitness.enums.Role.ADMIN && currentUser.getBranch() != null) {
            branchId = currentUser.getBranch().getBranchId();
        }

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

        long churnThisMonth = (branchId != null)
            ? memberRepository.countByStatusAndHomeBranchBranchId(Member.Status.DEACTIVATED, branchId)
            : memberRepository.countByStatus(Member.Status.DEACTIVATED);
        dto.setChurnThisMonth(churnThisMonth);
        dto.setChurnTrend(calculateTrend(churnThisMonth, 0)); // Stub last month

        List<com.fitness.entity.Classes> classesList = (branchId != null)
            ? classesRepository.findByBranchBranchId(branchId)
            : classesRepository.findAll();

        double totalOccupancy = 0;
        for (com.fitness.entity.Classes c : classesList) {
            long bookings = bookingRepository.countByFitnessClassClassIdAndBookingStatus(c.getClassId(), com.fitness.entity.ClassBooking.BookingStatus.CONFIRMED);
            int cap = c.getCapacity() > 0 ? c.getCapacity() : 1;
            totalOccupancy += (double) bookings / cap;
        }
        dto.setAvgClassOccupancy(classesList.isEmpty() ? 0.0 : (totalOccupancy / classesList.size()) * 100);

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
            point.setChurn((branchId != null)
                ? memberRepository.countByStatusAndHomeBranchBranchId(Member.Status.DEACTIVATED, branchId)
                : memberRepository.countByStatus(Member.Status.DEACTIVATED));
            analytics.add(point);
        }
        dto.setRevenueAnalytics(analytics);

        // 3. Top Classes
        dto.setTopClasses(classesList.stream().limit(5).map(c -> {
            ManagerDashboardDto.ClassUtilizationDto util = new ManagerDashboardDto.ClassUtilizationDto();
            util.setClassId(c.getClassId().intValue());
            util.setName(c.getClassName());
            long bookings = bookingRepository.countByFitnessClassClassIdAndBookingStatus(c.getClassId(), com.fitness.entity.ClassBooking.BookingStatus.CONFIRMED);
            int cap = c.getCapacity() > 0 ? c.getCapacity() : 1;
            util.setOccupancy((double) bookings / cap * 100);
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

    public String exportAnalyticsCsv() {
        return "Month,Revenue,NewJoins,Churn\n" + getDashboardStats(null).getRevenueAnalytics().stream()
            .map(r -> r.getMonth() + "," + r.getRevenue() + "," + r.getNewJoins() + "," + r.getChurn())
            .collect(Collectors.joining("\n"));
    }
}
