package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICouponService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICustomerService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.IOrderService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Order;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.OrderStatus;
import com.princz_mia.viaual04_gourmetgo_backend.exception.BusinessRuleException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.OrderDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.OrderStatusDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/orders")
public class OrderController {

    private final IOrderService orderService;
    private final ICustomerService customerService;
    private final ICouponService couponService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllOrders() {
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(new ApiResponse("Orders retrieved successfully", orders));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable UUID orderId) {
        OrderDto order = orderService.getOrder(orderId);
        return ResponseEntity.ok(new ApiResponse("Order retrieved successfully", order));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse> getCustomerOrders(@PathVariable UUID customerId) {
        List<OrderDto> orders = orderService.getCustomerOrders(customerId);
        return ResponseEntity.ok(new ApiResponse("Customer orders retrieved successfully", orders));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse> getRestaurantOrders(@PathVariable UUID restaurantId) {
        List<OrderDto> orders = orderService.getRestaurantOrders(restaurantId);
        return ResponseEntity.ok(new ApiResponse("Restaurant orders retrieved successfully", orders));
    }

    @GetMapping("/has-ordered/{restaurantId}")
    public ResponseEntity<ApiResponse> hasOrdered(@PathVariable UUID restaurantId) {
        Customer customer = customerService.getAuthenticatedCustomer();
        boolean hasOrdered = orderService.existsByCustomerIdAndRestaurantId(customer.getId(), restaurantId);
        
        if (!hasOrdered) {
            throw new BusinessRuleException("Customer has not ordered from this restaurant");
        }
        
        return ResponseEntity.ok(new ApiResponse("Customer has ordered from this restaurant", null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> placeOrder(@Valid @RequestBody OrderDto orderDto) {
        Customer customer = customerService.getAuthenticatedCustomer();
        Order order = orderService.placeOrder(customer, orderDto);
        OrderDto responseDto = orderService.convertToDto(order);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Order placed successfully", responseDto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderStatusDto statusDto) {
        OrderStatus newStatus = OrderStatus.valueOf(statusDto.getStatus());
        orderService.updateStatus(id, newStatus);
        return ResponseEntity.ok(new ApiResponse("Order status updated successfully", null));
    }
}
