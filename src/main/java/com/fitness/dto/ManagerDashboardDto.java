package com.fitness.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ManagerDashboardDto {
    private long activeMembers;
    private long newJoinsThisMonth;
    private double newJoinsTrend; // Percentage vs last month
    private long churnThisMonth;
    private long churnTrend;
    private double monthlyRevenue;
    private long classesThisWeek;
    private double avgClassOccupancy;
    
    private List<RevenuePoint> revenueAnalytics;
    private List<ClassUtilizationDto> topClasses;
    private List<DunningMemberDto> dunningQueue;

    @Data
    public static class RevenuePoint {
        private String month;
        private Double revenue;
        private Long newJoins;
        private Long churn;
    }

    @Data
    public static class ClassUtilizationDto {
        private Integer classId;
        private String name;
        private Double occupancy;
        private String fill;
    }

    @Data
    public static class DunningMemberDto {
        private Integer invoiceId;
        private String name;
        private String email;
        private Double outstandingAmount;
        private Integer daysOverdue;
        private String retryDate;
        private String status;
    }
}
