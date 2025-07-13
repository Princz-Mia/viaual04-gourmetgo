package com.princz_mia.viaual04_gourmetgo_backend.image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {
    Image findByProduct_Id(UUID productId);
    Image findByRestaurant_Id(UUID restaurantId);
}