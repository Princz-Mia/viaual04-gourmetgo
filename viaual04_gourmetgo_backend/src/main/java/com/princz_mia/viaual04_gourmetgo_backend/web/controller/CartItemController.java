package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICartItemService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICartService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICustomerService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Cart;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/cartItems")
@Slf4j
public class CartItemController {
    private final ICartItemService cartItemService;
    private final ICustomerService customerService;
    private final ICartService cartService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addItemToCart(
            @RequestParam UUID productId,
            @RequestParam(defaultValue="1") Integer quantity
    ) {
        LoggingUtils.logMethodEntry(log, "addItemToCart", "productId", productId, "quantity", quantity);
        long startTime = System.currentTimeMillis();
        
        try {
            Customer customer = customerService.getAuthenticatedCustomer();
            Cart cart = cartService.createCart(customer);
            cartItemService.addItemToCart(cart.getId(), productId, quantity);
            
            LoggingUtils.logBusinessEvent(log, "ITEM_ADDED_TO_CART", "productId", productId, "quantity", quantity, "customerId", customer.getId());
            LoggingUtils.logPerformance(log, "addItemToCart", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Add Item Success", null));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Failed to add item to cart - not found", e, "productId", productId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (JwtException e) {
            LoggingUtils.logSecurityEvent(log, "UNAUTHORIZED_CART_ACCESS", "productId", productId, "error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/by-product/{productId}")
    public ResponseEntity<ApiResponse> removeItemFromCart(
            @PathVariable UUID productId
    ) {
        LoggingUtils.logMethodEntry(log, "removeItemFromCart", "productId", productId);
        long startTime = System.currentTimeMillis();
        
        try {
            Customer customer = customerService.getAuthenticatedCustomer();
            Cart cart = cartService.createCart(customer);
            cartItemService.removeItemFromCart(cart.getId(), productId);
            
            LoggingUtils.logBusinessEvent(log, "ITEM_REMOVED_FROM_CART", "productId", productId, "customerId", customer.getId());
            LoggingUtils.logPerformance(log, "removeItemFromCart", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Remove Item Success", null));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Failed to remove item from cart", e, "productId", productId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/by-product/{productId}")
    public ResponseEntity<ApiResponse> updateItemQuantity(
            @PathVariable UUID productId,
            @RequestParam Integer quantity
    ) {
        LoggingUtils.logMethodEntry(log, "updateItemQuantity", "productId", productId, "quantity", quantity);
        long startTime = System.currentTimeMillis();
        
        try {
            Customer customer = customerService.getAuthenticatedCustomer();
            Cart cart = cartService.createCart(customer);
            cartItemService.updateItemQuantity(cart.getId(), productId, quantity);
            
            LoggingUtils.logBusinessEvent(log, "CART_ITEM_QUANTITY_UPDATED", "productId", productId, "quantity", quantity, "customerId", customer.getId());
            LoggingUtils.logPerformance(log, "updateItemQuantity", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Update Item Success", null));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Failed to update item quantity", e, "productId", productId, "quantity", quantity);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
}