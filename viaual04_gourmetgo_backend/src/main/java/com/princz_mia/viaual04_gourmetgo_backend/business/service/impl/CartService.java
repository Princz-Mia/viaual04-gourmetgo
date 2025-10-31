package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICartService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Cart;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.CartItem;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.CartItemRepository;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.CartRepository;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CartDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;

    @Override
    public Cart getCart(UUID id) {
        LoggingUtils.logMethodEntry(log, "getCart", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            Cart cart = cartRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
            BigDecimal totalAmount = cart.getTotalAmount();
            cart.setTotalAmount(totalAmount);
            
            LoggingUtils.logPerformance(log, "getCart", System.currentTimeMillis() - startTime);
            return cartRepository.save(cart);
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to get cart", e, "cartId", id);
            throw e;
        }
    }

    @Override
    @Transactional
    public Cart createCart(Customer customer) {
        LoggingUtils.logMethodEntry(log, "createCart", "customerId", customer.getId());
        long startTime = System.currentTimeMillis();
        
        try {
            Cart cart = Optional.ofNullable(getCartByCustomerId(customer.getId()))
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setCustomer(customer);
                        Cart savedCart = cartRepository.save(newCart);
                        LoggingUtils.logBusinessEvent(log, "CART_CREATED", "cartId", savedCart.getId(), "customerId", customer.getId());
                        return savedCart;
                    });
            
            LoggingUtils.logPerformance(log, "createCart", System.currentTimeMillis() - startTime);
            return cart;
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to create cart", e, "customerId", customer.getId());
            throw e;
        }
    }

    @Override
    @Transactional
    public void clearCart(UUID id) {
        LoggingUtils.logMethodEntry(log, "clearCart", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            Cart cart = Optional.of(getCart(id))
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
            cartItemRepository.deleteAllByCart_Id(id);
            cart.getItems().clear();
            
            LoggingUtils.logBusinessEvent(log, "CART_CLEARED", "cartId", id);
            LoggingUtils.logPerformance(log, "clearCart", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to clear cart", e, "cartId", id);
            throw e;
        }
    }

    @Override
    public BigDecimal getTotalPrice(UUID id) {
        LoggingUtils.logMethodEntry(log, "getTotalPrice", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            Cart cart = getCart(id);
            BigDecimal totalPrice = cart.getItems()
                    .stream()
                    .map(CartItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            LoggingUtils.logBusinessEvent(log, "CART_TOTAL_CALCULATED", "cartId", id, "totalPrice", totalPrice);
            LoggingUtils.logPerformance(log, "getTotalPrice", System.currentTimeMillis() - startTime);
            
            return totalPrice;
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to calculate total price", e, "cartId", id);
            throw e;
        }
    }

    @Override
    public Cart getCartByCustomerId(UUID customerId) {
        LoggingUtils.logMethodEntry(log, "getCartByCustomerId", "customerId", customerId);
        long startTime = System.currentTimeMillis();
        
        try {
            Cart cart = cartRepository.findByCustomer_Id(customerId);
            LoggingUtils.logPerformance(log, "getCartByCustomerId", System.currentTimeMillis() - startTime);
            return cart;
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to get cart by customer", e, "customerId", customerId);
            throw e;
        }
    }

    @Override
    public CartDto convertToDto(Cart cart) {
        LoggingUtils.logMethodEntry(log, "convertToDto", "cartId", cart.getId());
        return modelMapper.map(cart, CartDto.class);
    }
}