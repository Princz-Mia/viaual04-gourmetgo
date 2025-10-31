package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICartService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICustomerService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Cart;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CartDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/carts")
@Slf4j
public class CartController {

    private final ICartService cartService;
    private final ICustomerService customerService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCart(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "getCart", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            Cart cart = cartService.getCart(id);
            CartDto cartDto = cartService.convertToDto(cart);
            
            LoggingUtils.logBusinessEvent(log, "CART_RETRIEVED", "cartId", id);
            LoggingUtils.logPerformance(log, "getCart", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Success", cartDto));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Cart not found", e, "id", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/by-customer")
    public ResponseEntity<ApiResponse> getCartByCustomerId() {
        LoggingUtils.logMethodEntry(log, "getCartByCustomerId");
        long startTime = System.currentTimeMillis();
        
        try {
            Customer customer = customerService.getAuthenticatedCustomer();
            Cart cart = cartService.getCartByCustomerId(customer.getId());
            CartDto cartDto = cartService.convertToDto(cart);
            
            LoggingUtils.logBusinessEvent(log, "CUSTOMER_CART_RETRIEVED", "customerId", customer.getId());
            LoggingUtils.logPerformance(log, "getCartByCustomerId", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Success", cartDto));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Customer cart not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> clearCart(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "clearCart", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            cartService.clearCart(id);
            
            LoggingUtils.logBusinessEvent(log, "CART_CLEARED", "cartId", id);
            LoggingUtils.logPerformance(log, "clearCart", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Clear Cart Success", null));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Cart not found for clearing", e, "id", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/{id}/total")
    public ResponseEntity<ApiResponse> getTotalAmount(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "getTotalAmount", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            BigDecimal totalPrice = cartService.getTotalPrice(id);
            
            LoggingUtils.logBusinessEvent(log, "CART_TOTAL_CALCULATED", "cartId", id, "totalPrice", totalPrice);
            LoggingUtils.logPerformance(log, "getTotalAmount", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Total Price", totalPrice));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Cart not found for total calculation", e, "id", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
}