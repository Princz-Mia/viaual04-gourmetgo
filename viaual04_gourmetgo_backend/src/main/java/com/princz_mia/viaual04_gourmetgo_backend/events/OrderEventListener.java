package com.princz_mia.viaual04_gourmetgo_backend.events;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IStatisticsService;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderEventListener {
    
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    @EventListener
    public void handleOrderEvent(Object orderEvent) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(7);
            
            // Get order statistics
            Long todayOrders = orderRepository.countOrdersBetweenDates(today, today);
            
            // Get daily orders for chart
            Map<LocalDate, Long> dailyOrders = new HashMap<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                Long count = orderRepository.countOrdersBetweenDates(date, date);
                dailyOrders.put(date, count);
            }
            
            // Get orders by status
            List<Object[]> statusCounts = orderRepository.countOrdersByStatusGrouped(weekAgo, today);
            Map<String, Long> ordersByStatus = new HashMap<>();
            for (Object[] row : statusCounts) {
                ordersByStatus.put(row[0].toString(), (Long) row[1]);
            }
            
            // Get top products
            List<Object[]> topProductsData = orderRepository.findTopProductsGlobally(weekAgo, today);
            List<Map<String, Object>> topProducts = new ArrayList<>();
            for (Object[] row : topProductsData) {
                if (topProducts.size() >= 5) break;
                Map<String, Object> product = new HashMap<>();
                product.put("productName", row[0]);
                product.put("orderCount", row[1]);
                topProducts.add(product);
            }
            
            Map<String, Object> orderStats = new HashMap<>();
            orderStats.put("totalOrders", todayOrders);
            orderStats.put("dailyOrders", dailyOrders);
            orderStats.put("ordersByStatus", ordersByStatus);
            
            Map<String, Object> orderMessage = new HashMap<>();
            orderMessage.put("type", "ORDER_UPDATE");
            orderMessage.put("data", orderStats);
            
            Map<String, Object> productsMessage = new HashMap<>();
            productsMessage.put("type", "PRODUCTS_UPDATE");
            productsMessage.put("data", topProducts);
            
            messagingTemplate.convertAndSend("/topic/admin/statistics", orderMessage);
            messagingTemplate.convertAndSend("/topic/admin/statistics", productsMessage);
            
            // Send restaurant-specific updates - get restaurant data for each restaurant
            List<Object[]> restaurantOrders = orderRepository.findAll().stream()
                .filter(order -> order.getRestaurant() != null)
                .collect(Collectors.groupingBy(
                    order -> order.getRestaurant().getId(),
                    Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .collect(Collectors.toList());
            
            // Send updates for each restaurant
            for (Object[] restaurantData : restaurantOrders) {
                UUID restaurantId = (UUID) restaurantData[0];
                
                // Get restaurant-specific stats
                Long restaurantTotalOrders = orderRepository.countOrdersByRestaurantBetweenDates(restaurantId, weekAgo, today);
                List<Object[]> restaurantStatusCounts = orderRepository.countOrdersByRestaurantAndStatusGrouped(restaurantId, weekAgo, today);
                Map<String, Long> restaurantOrdersByStatus = new HashMap<>();
                for (Object[] row : restaurantStatusCounts) {
                    restaurantOrdersByStatus.put(row[0].toString(), (Long) row[1]);
                }
                
                Map<LocalDate, Long> restaurantDailyOrders = new HashMap<>();
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    Long count = orderRepository.countOrdersByRestaurantBetweenDates(restaurantId, date, date);
                    restaurantDailyOrders.put(date, count);
                }
                
                Long cancelledOrders = restaurantOrdersByStatus.getOrDefault("CANCELLED", 0L);
                Double cancellationRate = restaurantTotalOrders > 0 ? (cancelledOrders.doubleValue() / restaurantTotalOrders.doubleValue()) * 100 : 0.0;
                
                Map<String, Object> restaurantOrderStats = new HashMap<>();
                restaurantOrderStats.put("totalOrders", restaurantTotalOrders);
                restaurantOrderStats.put("ordersByStatus", restaurantOrdersByStatus);
                restaurantOrderStats.put("dailyOrders", restaurantDailyOrders);
                restaurantOrderStats.put("cancellationRate", cancellationRate);
                restaurantOrderStats.put("averagePreparationTime", 25.0);
                
                // Get restaurant revenue stats
                BigDecimal dailyRevenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDates(restaurantId, today, today)).orElse(BigDecimal.ZERO);
                BigDecimal weeklyRevenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDates(restaurantId, weekAgo, today)).orElse(BigDecimal.ZERO);
                BigDecimal monthlyRevenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDates(restaurantId, today.minusDays(30), today)).orElse(BigDecimal.ZERO);
                BigDecimal yearlyRevenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDates(restaurantId, today.minusDays(365), today)).orElse(BigDecimal.ZERO);
                
                Map<LocalDate, BigDecimal> dailyRevenueChart = new HashMap<>();
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    BigDecimal revenue = Optional.ofNullable(orderRepository.sumRevenueByRestaurantBetweenDates(restaurantId, date, date)).orElse(BigDecimal.ZERO);
                    dailyRevenueChart.put(date, revenue);
                }
                
                Map<String, Object> restaurantRevenueStats = new HashMap<>();
                restaurantRevenueStats.put("dailyRevenue", dailyRevenue);
                restaurantRevenueStats.put("weeklyRevenue", weeklyRevenue);
                restaurantRevenueStats.put("monthlyRevenue", monthlyRevenue);
                restaurantRevenueStats.put("yearlyRevenue", yearlyRevenue);
                restaurantRevenueStats.put("dailyRevenueChart", dailyRevenueChart);
                
                // Get restaurant top products
                List<Object[]> restaurantTopProductsData = orderRepository.findTopProductsByRestaurant(restaurantId, weekAgo, today);
                List<Map<String, Object>> restaurantTopProducts = restaurantTopProductsData.stream()
                    .limit(8)
                    .map(row -> {
                        Map<String, Object> product = new HashMap<>();
                        product.put("productName", row[0]);
                        product.put("orderCount", row[1]);
                        return product;
                    })
                    .collect(Collectors.toList());
                
                // Send WebSocket messages
                Map<String, Object> restaurantOrderMessage = new HashMap<>();
                restaurantOrderMessage.put("type", "ORDER_UPDATE");
                restaurantOrderMessage.put("restaurantId", restaurantId.toString());
                restaurantOrderMessage.put("data", restaurantOrderStats);
                
                Map<String, Object> restaurantRevenueMessage = new HashMap<>();
                restaurantRevenueMessage.put("type", "REVENUE_UPDATE");
                restaurantRevenueMessage.put("restaurantId", restaurantId.toString());
                restaurantRevenueMessage.put("data", restaurantRevenueStats);
                
                Map<String, Object> restaurantProductsMessage = new HashMap<>();
                restaurantProductsMessage.put("type", "PRODUCTS_UPDATE");
                restaurantProductsMessage.put("restaurantId", restaurantId.toString());
                restaurantProductsMessage.put("data", restaurantTopProducts);
                
                messagingTemplate.convertAndSend("/topic/restaurant/statistics", restaurantOrderMessage);
                messagingTemplate.convertAndSend("/topic/restaurant/statistics", restaurantRevenueMessage);
                messagingTemplate.convertAndSend("/topic/restaurant/statistics", restaurantProductsMessage);
            }
        } catch (Exception e) {
            // Ignore errors in event handling
        }
    }
}