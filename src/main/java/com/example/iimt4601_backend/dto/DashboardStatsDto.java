package com.example.iimt4601_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalUsers;
    private long newUsers;
    private long totalProducts;
    private long availableProducts;
    private long totalOrders;
    private long pendingOrders;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private long totalReviews;
    private Double averageRating;
    private Map<String, Long> orderStatusCounts;
    private Map<String, Integer> popularProducts;

    private StatCardDto orderStats;
    private StatCardDto revenueStats;
    private StatCardDto productStats;
    private StatCardDto userStats;
    private List<ChartDataDto> dailyOrdersChart;
    private List<ChartDataDto> dailyRevenueChart;
    private List<ChartDataDto> dailyUsersChart;
    private List<TopProductDto> topProducts;

    private String error;
}