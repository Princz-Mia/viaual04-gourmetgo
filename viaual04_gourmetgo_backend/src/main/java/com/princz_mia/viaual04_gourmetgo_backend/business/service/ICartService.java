package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Cart;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CartDto;

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
