package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.cart.Cart;
import com.princz_mia.viaual04_gourmetgo_backend.cart.CartRepository;
import com.princz_mia.viaual04_gourmetgo_backend.cart.ICartService;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.product.IProductService;
import com.princz_mia.viaual04_gourmetgo_backend.product.Product;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartItemService implements ICartItemService{

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    private final ICartService cartService;
    private final IProductService productService;

    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CartItemDto addItemToCart(UUID cartId, UUID productId, int quantity) {
        Cart cart = cartService.getCart(cartId);

        // cross‐restaurant check
        if (!cart.getItems().isEmpty()) {
            UUID existingRestId = cart.getItems().iterator().next().getProduct().getRestaurant().getId();
            UUID newRestId = productService.getProductById(productId).getRestaurant().getId();
            if (!existingRestId.equals(newRestId)) {
                throw new AppException("Cannot add from different restaurant", HttpStatus.BAD_REQUEST);
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

        return modelMapper.map(savedCartItem, CartItemDto.class);
    }

    @Override
    @Transactional
    public void removeItemFromCart(UUID cartId, UUID productId) {
        Cart cart = cartService.getCart(cartId);
        CartItem itemToRemove = getCartItem(cartId, productId);
        cart.removeItem(itemToRemove);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public CartItemDto updateItemQuantity(UUID cartId, UUID productId, int quantity) {
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
        return modelMapper.map(cartRepository.save(cart), CartItemDto.class);
    }

    @Override
    public CartItem getCartItem(UUID cartId, UUID productId) {
        Cart cart = cartService.getCart(cartId);
        return cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found", HttpStatus.NOT_FOUND));
    }
}
