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
public class AdminDashboardDto {
    
    private OnlineUsersDto onlineUsers;
    private TrafficStatsDto trafficStats;
    private OrderStatsDto orderStats;
    private PerformanceStatsDto performanceStats;
    private HappyHourStatsDto happyHourStats;
    private List<TopProductDto> topProducts;
    private UserRegistrationStatsDto userRegistrationStats;
    
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OnlineUsersDto {
        private Long activeUsers;
        private Long totalVisitors;
    }
    
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrafficStatsDto {
        private Map<String, Long> dailyVisits;
        private Long weeklyVisits;
        private Long monthlyVisits;
        private Long yearlyVisits;
    }
    
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderStatsDto {
        private Long totalOrders;
        private Map<String, Long> ordersByStatus;
        private Map<String, Long> dailyOrders;
        private Double averageDeliveryTime;
    }
    
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PerformanceStatsDto {
        private Long requestsPerHour;
        private Double averageResponseTime;
        private Double errorRate;
        private List<EndpointStatsDto> topEndpoints;
    }
    
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EndpointStatsDto {
        private String endpoint;
        private Long requestCount;
    }
    
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HappyHourStatsDto {
        private Long ordersInHappyHour;
        private Long ordersOutsideHappyHour;
        private Double happyHourIncrease;
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
    public static class UserRegistrationStatsDto {
        private Map<LocalDate, Long> dailyRegistrations;
        private Long weeklyRegistrations;
        private Long monthlyRegistrations;
        private Long yearlyRegistrations;
    }
}