package com.fitness.service;

import com.fitness.dto.DashboardFilterDTO;
import com.fitness.dto.ManagerDashboardDto;
import com.fitness.entity.Invoice;
import com.fitness.entity.Member;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

	private final MemberRepository memberRepo;
	private final InvoiceRepository invoiceRepo;
	private final ClassesRepository classesRepo;
	private final ClassBookingRepository bookingRepo;
	private final SystemUserRepository userRepo;

	private com.fitness.entity.SystemUser getCurrentUser() {
		String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepo.findByUsername(username).orElse(null);
	}

	@Cacheable(value = "dashboard_kpis", key = "#filter.hashCode()")
	public ManagerDashboardDto getDashboardStats(DashboardFilterDTO filter) {
		log.info("Generating Analytics Dashboard Stats with filter: {}", filter);
		ManagerDashboardDto dto = new ManagerDashboardDto();
		
		LocalDateTime now = filter.getEndDate() != null ? filter.getEndDate() : LocalDateTime.now();
		LocalDateTime start = filter.getStartDate() != null ? filter.getStartDate() : now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
		LocalDateTime startOfLastMonth = start.minusMonths(1);

		com.fitness.entity.SystemUser currentUser = getCurrentUser();
		Long branchId = filter.getBranchId();
		
		// Enforce Role-based visibility
		if (currentUser != null) {
			if (currentUser.getRole() == com.fitness.enums.Role.MANAGER || currentUser.getRole() == com.fitness.enums.Role.FRONT_DESK) {
				if (currentUser.getBranch() != null) {
					branchId = currentUser.getBranch().getBranchId(); // Force their branch
				}
			}
		}

		// 1. KPI: Active Members
		long activeCount = (branchId != null) 
			? memberRepo.countByStatusAndHomeBranchBranchId(Member.Status.ACTIVE, branchId)
			: memberRepo.countByStatus(Member.Status.ACTIVE);
		dto.setActiveMembers(activeCount);
		
		// 2. KPI: New Joins
		long newJoinsThisPeriod = (branchId != null)
			? memberRepo.countByCreatedAtBetweenAndHomeBranchBranchId(start, now, branchId)
			: memberRepo.countByCreatedAtBetween(start, now);
		
		long newJoinsLastPeriod = (branchId != null)
			? memberRepo.countByCreatedAtBetweenAndHomeBranchBranchId(startOfLastMonth, start, branchId)
			: memberRepo.countByCreatedAtBetween(startOfLastMonth, start);
			
		dto.setNewJoinsThisMonth(newJoinsThisPeriod);
		dto.setNewJoinsTrend(calculateTrend(newJoinsThisPeriod, newJoinsLastPeriod));

		// 3. KPI: Revenue
		java.math.BigDecimal revenue = (branchId != null)
			? invoiceRepo.sumRevenueByCreatedAtBetweenAndBranchId(start, now, branchId)
			: invoiceRepo.sumRevenueByCreatedAtBetween(start, now);
		dto.setMonthlyRevenue(revenue != null ? revenue.doubleValue() : 0.0);

		// 4. KPI: Churn
		long churnThisMonth = (branchId != null)
			? memberRepo.countByStatusAndHomeBranchBranchId(Member.Status.DEACTIVATED, branchId)
			: memberRepo.countByStatus(Member.Status.DEACTIVATED);
		dto.setChurnThisMonth(churnThisMonth);
		dto.setChurnTrend(calculateTrend(churnThisMonth, 0)); // Stub last month

		// KPI: MRR (stubbed as 80% of total revenue for demo purposes if not strictly tracked via recurring plan active state)
		dto.setMrr(revenue != null ? revenue.doubleValue() * 0.8 : 0.0);

		List<com.fitness.entity.Classes> classesList = (branchId != null)
			? classesRepo.findByBranchBranchId(branchId)
			: classesRepo.findAll();

		// 5. Occupancy Analytics
		double totalOccupancy = 0;
		for (com.fitness.entity.Classes c : classesList) {
			long bookings = bookingRepo.countByFitnessClassClassIdAndBookingStatus(c.getClassId(), com.fitness.entity.ClassBooking.BookingStatus.CONFIRMED);
			int cap = c.getCapacity() > 0 ? c.getCapacity() : 1;
			totalOccupancy += (double) bookings / cap;
		}
		dto.setAvgClassOccupancy(classesList.isEmpty() ? 0.0 : (totalOccupancy / classesList.size()) * 100);

		// 6. Revenue Analytics Chart (Last 6 Months)
		List<ManagerDashboardDto.RevenuePoint> analytics = new ArrayList<>();
		for (int i = 5; i >= 0; i--) {
			LocalDateTime mStart = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0);
			LocalDateTime mEnd = mStart.plusMonths(1);
			
			ManagerDashboardDto.RevenuePoint point = new ManagerDashboardDto.RevenuePoint();
			point.setMonth(mStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
			
			java.math.BigDecimal monthRev = (branchId != null)
				? invoiceRepo.sumRevenueByCreatedAtBetweenAndBranchId(mStart, mEnd, branchId)
				: invoiceRepo.sumRevenueByCreatedAtBetween(mStart, mEnd);
			point.setRevenue(monthRev != null ? monthRev.doubleValue() : 0.0);
			
			point.setNewJoins((branchId != null)
				? memberRepo.countByCreatedAtBetweenAndHomeBranchBranchId(mStart, mEnd, branchId)
				: memberRepo.countByCreatedAtBetween(mStart, mEnd));
				
			point.setChurn((branchId != null)
				? memberRepo.countByStatusAndHomeBranchBranchId(Member.Status.DEACTIVATED, branchId)
				: memberRepo.countByStatus(Member.Status.DEACTIVATED));
			analytics.add(point);
		}
		dto.setRevenueAnalytics(analytics);

		// 7. Top Classes Utilization
		dto.setTopClasses(classesList.stream().limit(5).map(c -> {
			ManagerDashboardDto.ClassUtilizationDto util = new ManagerDashboardDto.ClassUtilizationDto();
			util.setClassId(c.getClassId().intValue());
			util.setName(c.getClassName());
			long bookings = bookingRepo.countByFitnessClassClassIdAndBookingStatus(c.getClassId(), com.fitness.entity.ClassBooking.BookingStatus.CONFIRMED);
			int cap = c.getCapacity() > 0 ? c.getCapacity() : 1;
			util.setOccupancy((double) bookings / cap * 100);
			util.setFill("#2563EB"); // Can be determined by frontend
			return util;
		}).collect(Collectors.toList()));

		// 8. Dunning Queue Drill-down
		List<Invoice> overdueInvoices = (branchId != null)
			? invoiceRepo.findByStatusInAndBranchId(List.of(Invoice.Status.OVERDUE, Invoice.Status.PENDING, Invoice.Status.UNPAID), branchId)
			: invoiceRepo.findByStatusIn(List.of(Invoice.Status.OVERDUE, Invoice.Status.PENDING, Invoice.Status.UNPAID));
		
		dto.setDunningQueue(overdueInvoices.stream().limit(10).map(inv -> {
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
