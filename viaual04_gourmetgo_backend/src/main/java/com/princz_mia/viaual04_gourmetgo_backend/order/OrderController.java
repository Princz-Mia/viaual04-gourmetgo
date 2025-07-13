package com.princz_mia.viaual04_gourmetgo_backend.order;

import com.princz_mia.viaual04_gourmetgo_backend.coupon.Coupon;
import com.princz_mia.viaual04_gourmetgo_backend.coupon.CouponService;
import com.princz_mia.viaual04_gourmetgo_backend.customer.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.customer.ICustomerService;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.order_status.OrderStatus;
import com.princz_mia.viaual04_gourmetgo_backend.order_status.OrderStatusDto;
import com.princz_mia.viaual04_gourmetgo_backend.response.ApiResponse;
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
    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(new ApiResponse("Success", orders));
    }

    @GetMapping("/by-id/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable UUID orderId) {
        try {
            OrderDto order = orderService.getOrder(orderId);
            return ResponseEntity.ok(new ApiResponse("Success!", order));
        } catch (ResourceNotFoundException e) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<ApiResponse> getCustomerOrders(@PathVariable UUID customerId) {
        try {
            List<OrderDto> orders = orderService.getCustomerOrders(customerId);
            return ResponseEntity.ok(new ApiResponse("Success!", orders));
        } catch (ResourceNotFoundException e) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/by-restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse> getRestaurantOrders(@PathVariable UUID restaurantId) {
        try {
            List<OrderDto> orders = orderService.getRestaurantOrders(restaurantId);
            return ResponseEntity.ok(new ApiResponse("Success!", orders));
        } catch (ResourceNotFoundException e) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/has-ordered/{restaurantId}")
    public ResponseEntity<Void> hasOrdered(@PathVariable UUID restaurantId) {
        Customer customer = customerService.getAuthenticatedCustomer();
        if (orderService.existsByCustomerIdAndRestaurantId(customer.getId(), restaurantId)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping
    public ResponseEntity<ApiResponse> placeOrder(@RequestBody OrderDto req) {
        Customer cust = customerService.getAuthenticatedCustomer();
        Order order = orderService.placeOrder(cust, req);

        OrderDto dto = orderService.convertToDto(order);
        return ResponseEntity.ok(new ApiResponse("Order placed", dto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody OrderStatusDto dto
    ) {
        try {
            OrderStatus newStatus = OrderStatus.valueOf(dto.getStatus());
            orderService.updateStatus(id, newStatus);
            return ResponseEntity.ok(new ApiResponse("Success", null));
        } catch (ResourceNotFoundException e) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }
}
