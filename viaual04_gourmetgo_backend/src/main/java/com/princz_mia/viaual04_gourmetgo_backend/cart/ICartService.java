package com.princz_mia.viaual04_gourmetgo_backend.cart;

import com.princz_mia.viaual04_gourmetgo_backend.customer.Customer;

import java.math.BigDecimal;
import java.util.UUID;

public interface ICartService {

    Cart getCart(UUID id);

    Cart createCart(Customer customer);

    void clearCart(UUID id);

    BigDecimal getTotalPrice(UUID id);

    Cart getCartByCustomerId(UUID customerId);

    CartDto convertToDto(Cart cart);

}
