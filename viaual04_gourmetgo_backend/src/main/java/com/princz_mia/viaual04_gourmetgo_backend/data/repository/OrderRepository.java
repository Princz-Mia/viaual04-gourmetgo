package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByCustomer_Id(UUID customerId);

    List<Order> findByRestaurant_Id(UUID restaurantId);

    boolean existsByCustomer_IdAndRestaurant_Id(UUID customerId, UUID restaurantId);
}
