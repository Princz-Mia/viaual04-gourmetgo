package com.princz_mia.viaual04_gourmetgo_backend.web.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantDashboardDto {
    
    private OrderStatsDto orderStats;
    private RevenueStatsDto revenueStats;
    private List<TopProductDto> topProducts;
    private List<TopCategoryDto> topCategories;
    private CustomerStatsDto customerStats;
    private HappyHourStatsDto happyHourStats;
    
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderStatsDto {
        private Long totalOrders;
        private Map<String, Long> ordersByStatus;
        private Map<String, Long> dailyOrders;
        private Double averagePreparationTime;
        private Double cancellationRate;
    }
    
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RevenueStatsDto {
        private BigDecimal dailyRevenue;
        private BigDecimal weeklyRevenue;
        private BigDecimal monthlyRevenue;
        private BigDecimal yearlyRevenue;
        private Map<String, BigDecimal> dailyRevenueChart;
    }
    
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopProductDto {
        private String productName;
        private Long orderCount;
    }
    
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopCategoryDto {
        private String categoryName;
        private Long orderCount;
    }
    
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CustomerStatsDto {
        private Long totalCustomers;
        private Long repeatCustomers;
    }
    
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HappyHourStatsDto {
        private Long ordersInHappyHour;
        private Long ordersOutsideHappyHour;
        private BigDecimal revenueInHappyHour;
        private BigDecimal revenueOutsideHappyHour;
        private Double orderIncrease;
        private Double revenueIncrease;
    }
}