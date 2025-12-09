package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Order;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByCustomer_Id(UUID customerId);

    List<Order> findByRestaurant_Id(UUID restaurantId);

    boolean existsByCustomer_IdAndRestaurant_Id(UUID customerId, UUID restaurantId);
    
    // Statistics queries
    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.orderDate) BETWEEN :startDate AND :endDate")
    Long countOrdersBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.orderDate) BETWEEN :startDate AND :endDate AND o.status = :status")
    Long countOrdersByStatusBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("status") OrderStatus status);
    
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE DATE(o.orderDate) BETWEEN :startDate AND :endDate GROUP BY o.status")
    List<Object[]> countOrdersByStatusGrouped(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE DATE(o.orderDate) BETWEEN :startDate AND :endDate AND o.restaurant.id = :restaurantId AND o.status != 'CANCELLED'")
    BigDecimal sumRevenueByRestaurantBetweenDates(@Param("restaurantId") UUID restaurantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurant.id = :restaurantId AND DATE(o.orderDate) BETWEEN :startDate AND :endDate")
    Long countOrdersByRestaurantBetweenDates(@Param("restaurantId") UUID restaurantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.restaurant.id = :restaurantId AND DATE(o.orderDate) BETWEEN :startDate AND :endDate GROUP BY o.status")
    List<Object[]> countOrdersByRestaurantAndStatusGrouped(@Param("restaurantId") UUID restaurantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT p.name, SUM(oi.quantity) FROM Order o JOIN o.orderItems oi JOIN oi.product p " +
           "WHERE DATE(o.orderDate) BETWEEN :startDate AND :endDate " +
           "GROUP BY p.id, p.name ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopProductsGlobally(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT p.name, SUM(oi.quantity) FROM Order o JOIN o.orderItems oi JOIN oi.product p " +
           "WHERE o.restaurant.id = :restaurantId AND DATE(o.orderDate) BETWEEN :startDate AND :endDate " +
           "GROUP BY p.id, p.name ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopProductsByRestaurant(@Param("restaurantId") UUID restaurantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    Double getTotalRevenue();
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= CURRENT_DATE")
    Long countTodayOrders();
    
    // Customer statistics
    @Query("SELECT COUNT(DISTINCT o.customer.id) FROM Order o WHERE o.restaurant.id = :restaurantId AND DATE(o.orderDate) BETWEEN :startDate AND :endDate")
    Long countUniqueCustomersByRestaurant(@Param("restaurantId") UUID restaurantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(DISTINCT o.customer.id) FROM Order o WHERE o.restaurant.id = :restaurantId " +
           "AND o.customer.id IN (SELECT o2.customer.id FROM Order o2 WHERE o2.restaurant.id = :restaurantId " +
           "AND DATE(o2.orderDate) BETWEEN :startDate AND :endDate GROUP BY o2.customer.id HAVING COUNT(o2) > 1)")
    Long countRepeatCustomersByRestaurant(@Param("restaurantId") UUID restaurantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Category statistics
    @Query("SELECT pc.name, SUM(oi.quantity) FROM Order o JOIN o.orderItems oi JOIN oi.product p JOIN p.category pc " +
           "WHERE o.restaurant.id = :restaurantId AND DATE(o.orderDate) BETWEEN :startDate AND :endDate " +
           "GROUP BY pc.id, pc.name ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopCategoriesByRestaurant(@Param("restaurantId") UUID restaurantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(DISTINCT pc.id) FROM Order o JOIN o.orderItems oi JOIN oi.product p JOIN p.category pc " +
           "WHERE o.restaurant.id = :restaurantId AND DATE(o.orderDate) BETWEEN :startDate AND :endDate")
    Long countActiveCategoriesByRestaurant(@Param("restaurantId") UUID restaurantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Hourly statistics
    @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurant.id = :restaurantId AND o.orderDate BETWEEN :startDateTime AND :endDateTime")
    Long countOrdersByRestaurantBetweenDateTimes(@Param("restaurantId") UUID restaurantId, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.restaurant.id = :restaurantId AND o.orderDate BETWEEN :startDateTime AND :endDateTime AND o.status != 'CANCELLED'")
    BigDecimal sumRevenueByRestaurantBetweenDateTimes(@Param("restaurantId") UUID restaurantId, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);
    
    // Global hourly statistics
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startDateTime AND :endDateTime")
    Long countOrdersBetweenDateTimes(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

}
