package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IStatisticsService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.*;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.*;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.AdminDashboardDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantDashboardDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService implements IStatisticsService {
    
    private final OrderRepository orderRepository;
    private final VisitStatisticsRepository visitStatisticsRepository;
    private final RequestLogRepository requestLogRepository;
    private final ActiveSessionRepository activeSessionRepository;
    private final UserRepository userRepository;
    private final HappyHourRepository happyHourRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    public AdminDashboardDto getAdminDashboard(LocalDate startDate, LocalDate endDate) {
        return AdminDashboardDto.builder()
                .onlineUsers(getOnlineUsersStats())
                .trafficStats(getTrafficStats(startDate, endDate))
                .orderStats(getGlobalOrderStats(startDate, endDate))
                .performanceStats(getPerformanceStats())
                .happyHourStats(getGlobalHappyHourStats(startDate, endDate))
                .topProducts(getTopProductsGlobally(startDate, endDate))
                .userRegistrationStats(getUserRegistrationStats(startDate, endDate))
                .build();
    }
    
    @Override
    public RestaurantDashboardDto getRestaurantDashboard(UUID restaurantId, LocalDate startDate, LocalDate endDate) {
        return RestaurantDashboardDto.builder()
                .orderStats(getRestaurantOrderStats(restaurantId, startDate, endDate))
                .revenueStats(getRestaurantRevenueStats(restaurantId, startDate, endDate))
                .topProducts(getTopProductsByRestaurant(restaurantId, startDate, endDate))
                .topCategories(getTopCategoriesByRestaurant(restaurantId, startDate, endDate))
                .customerStats(getRestaurantCustomerStats(restaurantId, startDate, endDate))
                .happyHourStats(getRestaurantHappyHourStats(restaurantId, startDate, endDate))
                .build();
    }
    
    @Override
    @Transactional
    public void trackVisit(String sessionId, String userEmail, String ipAddress, String userAgent) {
        // Update or create active session
        ActiveSession session = activeSessionRepository.findBySessionId(sessionId)
                .orElse(ActiveSession.builder()
                        .sessionId(sessionId)
                        .userEmail(userEmail != null ? userEmail : "anonymous")
                        .createdAt(LocalDateTime.now())
                        .ipAddress(ipAddress)
                        .userAgent(userAgent)
                        .build());
        
        session.setLastActivity(LocalDateTime.now());
        activeSessionRepository.save(session);
        
        // Update daily visit statistics
        LocalDate today = LocalDate.now();
        VisitStatistics stats = visitStatisticsRepository.findByDate(today)
                .orElse(VisitStatistics.builder()
                        .date(today)
                        .totalVisits(0L)
                        .uniqueVisitors(0L)
                        .authenticatedUsers(0L)
                        .anonymousUsers(0L)
                        .build());
        
        stats.setTotalVisits(stats.getTotalVisits() + 1);
        if (userEmail != null) {
            stats.setAuthenticatedUsers(stats.getAuthenticatedUsers() + 1);
        } else {
            stats.setAnonymousUsers(stats.getAnonymousUsers() + 1);
        }
        
        visitStatisticsRepository.save(stats);
        
        // Notify admins of visit update
        notifyStatisticsUpdate("VISIT_UPDATE", getActiveUsersCount());
        
        // Send updated traffic stats
        List<VisitStatistics> recentStats = visitStatisticsRepository.findByDateBetweenOrderByDateDesc(
            today.minusDays(7), today);
        
        Map<LocalDate, Long> dailyVisits = recentStats.stream()
            .collect(Collectors.toMap(VisitStatistics::getDate, VisitStatistics::getTotalVisits));
        
        Map<String, Object> trafficData = new HashMap<>();
        trafficData.put("dailyVisits", dailyVisits);
        trafficData.put("weeklyVisits", visitStatisticsRepository.sumTotalVisitsBetweenDates(
            today.minusDays(7), today));
        
        notifyStatisticsUpdate("TRAFFIC_UPDATE", trafficData);
    }
    
    @Override
    @Transactional
    public void trackRequest(String endpoint, String method, Integer statusCode, Long responseTime, String userAgent, String ipAddress) {
        RequestLog log = RequestLog.builder()
                .timestamp(LocalDateTime.now())
                .endpoint(endpoint)
                .method(method)
                .statusCode(statusCode)
                .responseTime(responseTime)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();
        
        requestLogRepository.save(log);
        
        // Only notify for non-WebSocket requests to avoid loops
        if (!endpoint.contains("/ws")) {
            // Send current performance stats
            LocalDateTime hourAgo = LocalDateTime.now().minusHours(1);
            LocalDateTime now = LocalDateTime.now();
            
            Long requestsPerHour = requestLogRepository.countRequestsBetween(hourAgo, now);
            Double avgResponseTime = requestLogRepository.averageResponseTimeBetween(hourAgo, now);
            Double errorRate = requestLogRepository.errorRateBetween(hourAgo, now);
            
            List<Object[]> topEndpointsData = requestLogRepository.topEndpointsBetween(hourAgo, now);
            List<Map<String, Object>> topEndpoints = topEndpointsData.stream()
                .limit(5)
                .map(row -> {
                    Map<String, Object> endpointData = new HashMap<>();
                    endpointData.put("endpoint", row[0]);
                    endpointData.put("requestCount", row[1]);
                    return endpointData;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> perfStats = new HashMap<>();
            perfStats.put("requestsPerHour", requestsPerHour != null ? requestsPerHour : 0L);
            perfStats.put("averageResponseTime", avgResponseTime != null ? avgResponseTime : 0.0);
            perfStats.put("errorRate", errorRate != null ? errorRate : 0.0);
            perfStats.put("topEndpoints", topEndpoints);
            
            notifyStatisticsUpdate("PERFORMANCE_UPDATE", perfStats);
            
            // Send restaurant revenue updates for all restaurants
            List<UUID> restaurantIds = orderRepository.findAll().stream()
                .filter(order -> order.getRestaurant() != null)
                .map(order -> order.getRestaurant().getId())
                .distinct()
                .collect(Collectors.toList());
            
            LocalDate today = LocalDate.now();
            for (UUID restaurantId : restaurantIds) {
                BigDecimal dailyRevenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDates(restaurantId, today, today)).orElse(BigDecimal.ZERO);
                BigDecimal weeklyRevenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDates(restaurantId, today.minusDays(7), today)).orElse(BigDecimal.ZERO);
                BigDecimal monthlyRevenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDates(restaurantId, today.minusDays(30), today)).orElse(BigDecimal.ZERO);
                BigDecimal yearlyRevenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDates(restaurantId, today.minusDays(365), today)).orElse(BigDecimal.ZERO);
                
                Map<String, Object> revenueData = new HashMap<>();
                revenueData.put("dailyRevenue", dailyRevenue);
                revenueData.put("weeklyRevenue", weeklyRevenue);
                revenueData.put("monthlyRevenue", monthlyRevenue);
                revenueData.put("yearlyRevenue", yearlyRevenue);
                
                Map<String, Object> restaurantRevenueMessage = new HashMap<>();
                restaurantRevenueMessage.put("type", "REVENUE_UPDATE");
                restaurantRevenueMessage.put("restaurantId", restaurantId.toString());
                restaurantRevenueMessage.put("data", revenueData);
                
                messagingTemplate.convertAndSend("/topic/restaurant/statistics", restaurantRevenueMessage);
            }
        }
    }
    
    @Override
    @Transactional
    public void updateSessionActivity(String sessionId) {
        activeSessionRepository.findBySessionId(sessionId)
                .ifPresent(session -> {
                    session.setLastActivity(LocalDateTime.now());
                    activeSessionRepository.save(session);
                });
    }
    
    @Override
    @Transactional
    public void removeSession(String sessionId) {
        activeSessionRepository.deleteBySessionId(sessionId);
        
        // Notify admins of session removal
        notifyStatisticsUpdate("VISIT_UPDATE", getActiveUsersCount());
        
        // Send updated traffic stats
        LocalDate currentDate = LocalDate.now();
        List<VisitStatistics> recentStats = visitStatisticsRepository.findByDateBetweenOrderByDateDesc(
            currentDate.minusDays(7), currentDate);
        
        Map<LocalDate, Long> dailyVisits = recentStats.stream()
            .collect(Collectors.toMap(VisitStatistics::getDate, VisitStatistics::getTotalVisits));
        
        Map<String, Object> trafficData = new HashMap<>();
        trafficData.put("dailyVisits", dailyVisits);
        trafficData.put("weeklyVisits", visitStatisticsRepository.sumTotalVisitsBetweenDates(
            currentDate.minusDays(7), currentDate));
        
        notifyStatisticsUpdate("TRAFFIC_UPDATE", trafficData);
    }
    
    @Override
    public Long getActiveUsersCount() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        return activeSessionRepository.countActiveSessions(threshold);
    }
    
    private AdminDashboardDto.OnlineUsersDto getOnlineUsersStats() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        Long activeUsers = activeSessionRepository.countActiveSessions(threshold);
        
        return AdminDashboardDto.OnlineUsersDto.builder()
                .activeUsers(activeUsers)
                .totalVisitors(activeUsers) // Simplified for now
                .build();
    }
    
    private AdminDashboardDto.TrafficStatsDto getTrafficStats(LocalDate startDate, LocalDate endDate) {
        List<VisitStatistics> stats = visitStatisticsRepository.findByDateBetweenOrderByDateDesc(startDate, endDate);
        
        Map<String, Long> dailyVisits = new LinkedHashMap<>();
        
        // If same day, generate hourly data
        if (startDate.equals(endDate)) {
            for (int hour = 0; hour < 24; hour++) {
                LocalDateTime hourStart = startDate.atTime(hour, 0);
                LocalDateTime hourEnd = startDate.atTime(hour, 59, 59);
                
                // Get actual hourly visit count from active sessions or request logs
                Long hourlyVisits = requestLogRepository.countRequestsBetween(hourStart, hourEnd);
                if (hourlyVisits == null) hourlyVisits = 0L;
                
                dailyVisits.put(hourStart.toString(), hourlyVisits);
            }
        } else {
            // Multi-day: generate daily data with 0 for missing days
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                VisitStatistics dayStats = stats.stream()
                    .filter(s -> s.getDate().equals(current))
                    .findFirst().orElse(null);
                Long visits = dayStats != null ? dayStats.getTotalVisits() : 0L;
                dailyVisits.put(current.toString(), visits);
                current = current.plusDays(1);
            }
        }
        
        LocalDate weekStart = LocalDate.now().minusDays(7);
        LocalDate monthStart = LocalDate.now().minusDays(30);
        LocalDate yearStart = LocalDate.now().minusDays(365);
        
        return AdminDashboardDto.TrafficStatsDto.builder()
                .dailyVisits(dailyVisits)
                .weeklyVisits(Optional.ofNullable(visitStatisticsRepository.sumTotalVisitsBetweenDates(weekStart, LocalDate.now())).orElse(0L))
                .monthlyVisits(Optional.ofNullable(visitStatisticsRepository.sumTotalVisitsBetweenDates(monthStart, LocalDate.now())).orElse(0L))
                .yearlyVisits(Optional.ofNullable(visitStatisticsRepository.sumTotalVisitsBetweenDates(yearStart, LocalDate.now())).orElse(0L))
                .build();
    }
    
    private AdminDashboardDto.OrderStatsDto getGlobalOrderStats(LocalDate startDate, LocalDate endDate) {
        Long totalOrders = orderRepository.countOrdersBetweenDates(startDate, endDate);
        
        List<Object[]> statusCounts = orderRepository.countOrdersByStatusGrouped(startDate, endDate);
        Map<String, Long> ordersByStatus = statusCounts.stream()
                .collect(Collectors.toMap(
                        row -> ((OrderStatus) row[0]).name(),
                        row -> (Long) row[1]
                ));
        
        // Generate daily orders for chart
        Map<String, Long> dailyOrders = new LinkedHashMap<>();
        
        // If same day, generate hourly data
        if (startDate.equals(endDate)) {
            for (int hour = 0; hour < 24; hour++) {
                LocalDateTime hourStart = startDate.atTime(hour, 0);
                LocalDateTime hourEnd = startDate.atTime(hour, 59, 59);
                Long count = orderRepository.countOrdersBetweenDateTimes(hourStart, hourEnd);
                if (count == null) count = 0L;
                dailyOrders.put(hourStart.toString(), count);
            }
        } else {
            // Multi-day: generate daily data
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                Long count = orderRepository.countOrdersBetweenDates(current, current);
                dailyOrders.put(current.toString(), count);
                current = current.plusDays(1);
            }
        }
        
        return AdminDashboardDto.OrderStatsDto.builder()
                .totalOrders(totalOrders)
                .ordersByStatus(ordersByStatus)
                .dailyOrders(dailyOrders)
                .averageDeliveryTime(24.5) // Placeholder
                .build();
    }
    
    private AdminDashboardDto.PerformanceStatsDto getPerformanceStats() {
        LocalDateTime hourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime now = LocalDateTime.now();
        
        Long requestsPerHour = requestLogRepository.countRequestsBetween(hourAgo, now);
        Double avgResponseTime = requestLogRepository.averageResponseTimeBetween(hourAgo, now);
        Double errorRate = requestLogRepository.errorRateBetween(hourAgo, now);
        
        List<Object[]> topEndpointsData = requestLogRepository.topEndpointsBetween(hourAgo, now);
        List<AdminDashboardDto.EndpointStatsDto> topEndpoints = topEndpointsData.stream()
                .limit(5)
                .map(row -> AdminDashboardDto.EndpointStatsDto.builder()
                        .endpoint((String) row[0])
                        .requestCount((Long) row[1])
                        .build())
                .collect(Collectors.toList());
        
        return AdminDashboardDto.PerformanceStatsDto.builder()
                .requestsPerHour(Optional.ofNullable(requestsPerHour).orElse(0L))
                .averageResponseTime(Optional.ofNullable(avgResponseTime).orElse(0.0))
                .errorRate(Optional.ofNullable(errorRate).orElse(0.0))
                .topEndpoints(topEndpoints)
                .build();
    }
    
    private AdminDashboardDto.HappyHourStatsDto getGlobalHappyHourStats(LocalDate startDate, LocalDate endDate) {
        // Simplified implementation - would need more complex logic for actual happy hour detection
        return AdminDashboardDto.HappyHourStatsDto.builder()
                .ordersInHappyHour(0L)
                .ordersOutsideHappyHour(0L)
                .happyHourIncrease(0.0)
                .build();
    }
    
    private List<AdminDashboardDto.TopProductDto> getTopProductsGlobally(LocalDate startDate, LocalDate endDate) {
        List<Object[]> topProductsData = orderRepository.findTopProductsGlobally(startDate, endDate);
        
        return topProductsData.stream()
                .limit(10)
                .map(row -> AdminDashboardDto.TopProductDto.builder()
                        .productName((String) row[0])
                        .orderCount((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }
    
    private AdminDashboardDto.UserRegistrationStatsDto getUserRegistrationStats(LocalDate startDate, LocalDate endDate) {
        // Simplified implementation - would need user creation date tracking
        return AdminDashboardDto.UserRegistrationStatsDto.builder()
                .dailyRegistrations(new HashMap<>())
                .weeklyRegistrations(0L)
                .monthlyRegistrations(0L)
                .yearlyRegistrations(0L)
                .build();
    }
    
    private RestaurantDashboardDto.OrderStatsDto getRestaurantOrderStats(UUID restaurantId, LocalDate startDate, LocalDate endDate) {
        Long totalOrders = orderRepository.countOrdersByRestaurantBetweenDates(restaurantId, startDate, endDate);
        
        List<Object[]> statusCounts = orderRepository.countOrdersByRestaurantAndStatusGrouped(restaurantId, startDate, endDate);
        Map<String, Long> ordersByStatus = statusCounts.stream()
                .collect(Collectors.toMap(
                        row -> ((OrderStatus) row[0]).name(),
                        row -> (Long) row[1]
                ));
        
        Map<String, Long> dailyOrders = new LinkedHashMap<>();
        
        // If same day, generate hourly data
        if (startDate.equals(endDate)) {
            for (int hour = 0; hour < 24; hour++) {
                LocalDateTime hourStart = startDate.atTime(hour, 0);
                LocalDateTime hourEnd = startDate.atTime(hour, 59, 59);
                Long count = orderRepository.countOrdersByRestaurantBetweenDateTimes(restaurantId, hourStart, hourEnd);
                dailyOrders.put(hourStart.toString(), count);
            }
        } else {
            // Multi-day: generate daily data
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                Long count = orderRepository.countOrdersByRestaurantBetweenDates(restaurantId, current, current);
                dailyOrders.put(current.toString(), count);
                current = current.plusDays(1);
            }
        }
        
        Long cancelledOrders = ordersByStatus.getOrDefault("CANCELLED", 0L);
        Double cancellationRate = totalOrders > 0 ? (cancelledOrders.doubleValue() / totalOrders.doubleValue()) * 100 : 0.0;
        
        return RestaurantDashboardDto.OrderStatsDto.builder()
                .totalOrders(totalOrders)
                .ordersByStatus(ordersByStatus)
                .dailyOrders(dailyOrders)
                .averagePreparationTime(25.0) // Placeholder
                .cancellationRate(cancellationRate)
                .build();
    }
    
    private RestaurantDashboardDto.RevenueStatsDto getRestaurantRevenueStats(UUID restaurantId, LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(7);
        LocalDate monthStart = today.minusDays(30);
        LocalDate yearStart = today.minusDays(365);
        
        // Always recalculate revenue to ensure cancelled orders are excluded
        BigDecimal dailyRevenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDates(restaurantId, today, today)).orElse(BigDecimal.ZERO);
        BigDecimal weeklyRevenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDates(restaurantId, weekStart, today)).orElse(BigDecimal.ZERO);
        BigDecimal monthlyRevenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDates(restaurantId, monthStart, today)).orElse(BigDecimal.ZERO);
        BigDecimal yearlyRevenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDates(restaurantId, yearStart, today)).orElse(BigDecimal.ZERO);
        
        Map<String, BigDecimal> dailyRevenueChart = new LinkedHashMap<>();
        
        // If same day, generate hourly data
        if (startDate.equals(endDate)) {
            for (int hour = 0; hour < 24; hour++) {
                LocalDateTime hourStart = startDate.atTime(hour, 0);
                LocalDateTime hourEnd = startDate.atTime(hour, 59, 59);
                BigDecimal revenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDateTimes(restaurantId, hourStart, hourEnd)).orElse(BigDecimal.ZERO);
                dailyRevenueChart.put(hourStart.toString(), revenue);
            }
        } else {
            // Multi-day: generate daily data
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                BigDecimal revenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDates(restaurantId, current, current)).orElse(BigDecimal.ZERO);
                dailyRevenueChart.put(current.toString(), revenue);
                current = current.plusDays(1);
            }
        }
        
        return RestaurantDashboardDto.RevenueStatsDto.builder()
                .dailyRevenue(dailyRevenue)
                .weeklyRevenue(weeklyRevenue)
                .monthlyRevenue(monthlyRevenue)
                .yearlyRevenue(yearlyRevenue)
                .dailyRevenueChart(dailyRevenueChart)
                .build();
    }
    
    private List<RestaurantDashboardDto.TopProductDto> getTopProductsByRestaurant(UUID restaurantId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> topProductsData = orderRepository.findTopProductsByRestaurant(restaurantId, startDate, endDate);
        
        return topProductsData.stream()
                .limit(10)
                .map(row -> RestaurantDashboardDto.TopProductDto.builder()
                        .productName((String) row[0])
                        .orderCount((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<RestaurantDashboardDto.TopCategoryDto> getTopCategoriesByRestaurant(UUID restaurantId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> topCategoriesData = orderRepository.findTopCategoriesByRestaurant(restaurantId, startDate, endDate);
        
        return topCategoriesData.stream()
                .limit(10)
                .map(row -> RestaurantDashboardDto.TopCategoryDto.builder()
                        .categoryName((String) row[0])
                        .orderCount((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }
    
    private RestaurantDashboardDto.CustomerStatsDto getRestaurantCustomerStats(UUID restaurantId, LocalDate startDate, LocalDate endDate) {
        Long totalCustomers = orderRepository.countUniqueCustomersByRestaurant(restaurantId, startDate, endDate);
        Long repeatCustomers = orderRepository.countRepeatCustomersByRestaurant(restaurantId, startDate, endDate);
        
        return RestaurantDashboardDto.CustomerStatsDto.builder()
                .totalCustomers(totalCustomers != null ? totalCustomers : 0L)
                .repeatCustomers(repeatCustomers != null ? repeatCustomers : 0L)
                .build();
    }
    
    private RestaurantDashboardDto.HappyHourStatsDto getRestaurantHappyHourStats(UUID restaurantId, LocalDate startDate, LocalDate endDate) {
        // Simplified implementation
        return RestaurantDashboardDto.HappyHourStatsDto.builder()
                .ordersInHappyHour(0L)
                .ordersOutsideHappyHour(0L)
                .revenueInHappyHour(BigDecimal.ZERO)
                .revenueOutsideHappyHour(BigDecimal.ZERO)
                .orderIncrease(0.0)
                .revenueIncrease(0.0)
                .build();
    }
    
    private void notifyStatisticsUpdate(String type, Object data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", type);
            message.put("data", data);
            messagingTemplate.convertAndSend("/topic/admin/statistics", message);
        } catch (Exception e) {
            log.warn("Failed to send statistics update notification", e);
        }
    }
}