package com.princz_mia.viaual04_gourmetgo_backend.cart;

import com.princz_mia.viaual04_gourmetgo_backend.cart_item.CartItem;
import com.princz_mia.viaual04_gourmetgo_backend.cart_item.CartItemRepository;
import com.princz_mia.viaual04_gourmetgo_backend.customer.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    private final ModelMapper modelMapper;

    @Override
    public Cart getCart(UUID id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found", HttpStatus.NOT_FOUND));
        BigDecimal totalAmount = cart.getTotalAmount();
        cart.setTotalAmount(totalAmount);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart createCart(Customer customer) {
        return Optional.ofNullable(getCartByCustomerId(customer.getId()))
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setCustomer(customer);
                    return cartRepository.save(cart);
                });
    }

    @Override
    @Transactional
    public void clearCart(UUID id) {
        Cart cart = Optional.of(getCart(id))
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found", HttpStatus.NOT_FOUND));
        cartItemRepository.deleteAllByCart_Id(id);
        cart.getItems().clear();
        //cartRepository.deleteById(id);
    }

    @Override
    public BigDecimal getTotalPrice(UUID id) {
        Cart cart = getCart(id);
        return cart.getItems()
                .stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Cart getCartByCustomerId(UUID customerId) {
        return cartRepository.findByCustomer_Id(customerId);
    }

    @Override
    public CartDto convertToDto(Cart cart) {
        return modelMapper.map(cart, CartDto.class);
    }
}
