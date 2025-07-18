package com.princz_mia.viaual04_gourmetgo_backend.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    Cart findByCustomer_Id(UUID customerId);
}
