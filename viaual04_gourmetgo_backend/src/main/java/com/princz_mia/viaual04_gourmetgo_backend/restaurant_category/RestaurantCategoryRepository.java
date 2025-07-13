package com.princz_mia.viaual04_gourmetgo_backend.restaurant_category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantCategoryRepository extends JpaRepository<RestaurantCategory, UUID> {
    Optional<RestaurantCategory> findByName(String name);
}
