package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICouponService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICustomerService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.IOrderService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Order;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.OrderStatus;
import com.princz_mia.viaual04_gourmetgo_backend.exception.BusinessRuleException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.OrderDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.OrderStatusDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/orders")
@Slf4j
public class OrderController {

    private final IOrderService orderService;
    private final ICustomerService customerService;
    private final ICouponService couponService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllOrders() {
        LoggingUtils.logMethodEntry(log, "getAllOrders");
        long startTime = System.currentTimeMillis();
        
        List<OrderDto> orders = orderService.getAllOrders();
        LoggingUtils.logBusinessEvent(log, "ORDERS_RETRIEVED", "count", orders.size());
        LoggingUtils.logPerformance(log, "getAllOrders", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Orders retrieved successfully", orders));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable UUID orderId) {
        LoggingUtils.logMethodEntry(log, "getOrderById", "orderId", orderId);
        long startTime = System.currentTimeMillis();
        
        OrderDto order = orderService.getOrder(orderId);
        LoggingUtils.logBusinessEvent(log, "ORDER_RETRIEVED", "orderId", orderId);
        LoggingUtils.logPerformance(log, "getOrderById", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Order retrieved successfully", order));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse> getCustomerOrders(@PathVariable UUID customerId) {
        LoggingUtils.logMethodEntry(log, "getCustomerOrders", "customerId", customerId);
        long startTime = System.currentTimeMillis();
        
        List<OrderDto> orders = orderService.getCustomerOrders(customerId);
        LoggingUtils.logBusinessEvent(log, "CUSTOMER_ORDERS_RETRIEVED", "customerId", customerId, "count", orders.size());
        LoggingUtils.logPerformance(log, "getCustomerOrders", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Customer orders retrieved successfully", orders));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse> getRestaurantOrders(@PathVariable UUID restaurantId) {
        LoggingUtils.logMethodEntry(log, "getRestaurantOrders", "restaurantId", restaurantId);
        long startTime = System.currentTimeMillis();
        
        List<OrderDto> orders = orderService.getRestaurantOrders(restaurantId);
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_ORDERS_RETRIEVED", "restaurantId", restaurantId, "count", orders.size());
        LoggingUtils.logPerformance(log, "getRestaurantOrders", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Restaurant orders retrieved successfully", orders));
    }

    @GetMapping("/has-ordered/{restaurantId}")
    public ResponseEntity<ApiResponse> hasOrdered(@PathVariable UUID restaurantId) {
        LoggingUtils.logMethodEntry(log, "hasOrdered", "restaurantId", restaurantId);
        long startTime = System.currentTimeMillis();
        
        Customer customer = customerService.getAuthenticatedCustomer();
        boolean hasOrdered = orderService.existsByCustomerIdAndRestaurantId(customer.getId(), restaurantId);
        
        if (!hasOrdered) {
            LoggingUtils.logBusinessEvent(log, "CUSTOMER_HAS_NOT_ORDERED", "customerId", customer.getId(), "restaurantId", restaurantId);
            throw new BusinessRuleException("Customer has not ordered from this restaurant");
        }
        
        LoggingUtils.logBusinessEvent(log, "CUSTOMER_HAS_ORDERED", "customerId", customer.getId(), "restaurantId", restaurantId);
        LoggingUtils.logPerformance(log, "hasOrdered", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Customer has ordered from this restaurant", null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> placeOrder(@Valid @RequestBody OrderDto orderDto) {
        LoggingUtils.logMethodEntry(log, "placeOrder");
        long startTime = System.currentTimeMillis();
        
        Customer customer = customerService.getAuthenticatedCustomer();
        Order order = orderService.placeOrder(customer, orderDto);
        OrderDto responseDto = orderService.convertToDto(order);
        
        LoggingUtils.logBusinessEvent(log, "ORDER_PLACED_VIA_API", "orderId", order.getId(), "customerId", customer.getId());
        LoggingUtils.logPerformance(log, "placeOrder", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Order placed successfully", responseDto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderStatusDto statusDto) {
        LoggingUtils.logMethodEntry(log, "updateOrderStatus", "orderId", id, "status", statusDto.getStatus());
        long startTime = System.currentTimeMillis();
        
        OrderStatus newStatus = OrderStatus.valueOf(statusDto.getStatus());
        orderService.updateStatus(id, newStatus);
        
        LoggingUtils.logBusinessEvent(log, "ORDER_STATUS_UPDATED", "orderId", id, "newStatus", newStatus);
        LoggingUtils.logPerformance(log, "updateOrderStatus", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Order status updated successfully", null));
    }
}