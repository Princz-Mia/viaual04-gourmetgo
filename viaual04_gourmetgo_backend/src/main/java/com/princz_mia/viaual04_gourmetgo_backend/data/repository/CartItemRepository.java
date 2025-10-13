package com.princz_mia.viaual04_gourmetgo_backend.cart_item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    CartItem findByCart_IdAndProduct_Id(UUID cartId, UUID productId);
    void deleteAllByCart_Id(UUID id);
}
