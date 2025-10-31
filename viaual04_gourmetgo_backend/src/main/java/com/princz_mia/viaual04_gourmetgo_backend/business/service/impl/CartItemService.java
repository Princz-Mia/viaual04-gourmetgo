package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICartItemService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICartService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.IProductService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Cart;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.CartItem;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Product;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.CartItemRepository;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.CartRepository;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ErrorType;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CartItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartItemService implements ICartItemService{

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ICartService cartService;
    private final IProductService productService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CartItemDto addItemToCart(UUID cartId, UUID productId, int quantity) {
        LoggingUtils.logMethodEntry(log, "addItemToCart", "cartId", cartId, "productId", productId, "quantity", quantity);
        long startTime = System.currentTimeMillis();
        
        try {
            Cart cart = cartService.getCart(cartId);

            // cross‐restaurant check
            if (!cart.getItems().isEmpty()) {
                UUID existingRestId = cart.getItems().iterator().next().getProduct().getRestaurant().getId();
                UUID newRestId = productService.getProductById(productId).getRestaurant().getId();
                if (!existingRestId.equals(newRestId)) {
                    throw new AppException("Cannot add from different restaurant", ErrorType.BUSINESS_RULE_VIOLATION);
                }
            }

            Product product = productService.getProductById(productId);
            CartItem cartItem = cart.getItems()
                    .stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .orElse(new CartItem());
            if (cartItem.getId() == null) {
                cartItem.setCart(cart);
                cartItem.setProduct(product);
                cartItem.setQuantity(quantity);
                cartItem.setUnitPrice(product.getPrice());
            } else {
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
            }
            cartItem.setTotalPrice();
            CartItem savedCartItem = cartItemRepository.save(cartItem);

            cart.addItem(savedCartItem);
            cartRepository.save(cart);
            
            LoggingUtils.logBusinessEvent(log, "CART_ITEM_ADDED", "cartId", cartId, "productId", productId, "quantity", quantity);
            LoggingUtils.logPerformance(log, "addItemToCart", System.currentTimeMillis() - startTime);
            
            return modelMapper.map(savedCartItem, CartItemDto.class);
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to add item to cart", e, "cartId", cartId, "productId", productId, "quantity", quantity);
            throw e;
        }
    }

    @Override
    @Transactional
    public void removeItemFromCart(UUID cartId, UUID productId) {
        LoggingUtils.logMethodEntry(log, "removeItemFromCart", "cartId", cartId, "productId", productId);
        long startTime = System.currentTimeMillis();
        
        try {
            Cart cart = cartService.getCart(cartId);
            CartItem itemToRemove = getCartItem(cartId, productId);
            cart.removeItem(itemToRemove);
            cartRepository.save(cart);
            
            LoggingUtils.logBusinessEvent(log, "CART_ITEM_REMOVED", "cartId", cartId, "productId", productId);
            LoggingUtils.logPerformance(log, "removeItemFromCart", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to remove item from cart", e, "cartId", cartId, "productId", productId);
            throw e;
        }
    }

    @Override
    @Transactional
    public CartItemDto updateItemQuantity(UUID cartId, UUID productId, int quantity) {
        LoggingUtils.logMethodEntry(log, "updateItemQuantity", "cartId", cartId, "productId", productId, "quantity", quantity);
        long startTime = System.currentTimeMillis();
        
        try {
            Cart cart = cartService.getCart(cartId);

            if (quantity <= 0) {
                removeItemFromCart(cartId, productId);
            }

            cart.getItems()
                    .stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setQuantity(quantity);
                        item.setUnitPrice(item.getProduct().getPrice());
                        item.setTotalPrice();
                    });
            BigDecimal totalAmount = cart.getItems()
                    .stream()
                    .map(CartItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            cart.setTotalAmount(totalAmount);
            
            LoggingUtils.logBusinessEvent(log, "CART_ITEM_QUANTITY_UPDATED", "cartId", cartId, "productId", productId, "quantity", quantity);
            LoggingUtils.logPerformance(log, "updateItemQuantity", System.currentTimeMillis() - startTime);
            
            return modelMapper.map(cartRepository.save(cart), CartItemDto.class);
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to update item quantity", e, "cartId", cartId, "productId", productId, "quantity", quantity);
            throw e;
        }
    }

    @Override
    public CartItem getCartItem(UUID cartId, UUID productId) {
        LoggingUtils.logMethodEntry(log, "getCartItem", "cartId", cartId, "productId", productId);
        long startTime = System.currentTimeMillis();
        
        try {
            Cart cart = cartService.getCart(cartId);
            CartItem cartItem = cart.getItems()
                    .stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
            
            LoggingUtils.logPerformance(log, "getCartItem", System.currentTimeMillis() - startTime);
            return cartItem;
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to get cart item", e, "cartId", cartId, "productId", productId);
            throw e;
        }
    }
}