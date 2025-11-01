package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByName(String name);
    
    List<Product> findByCategory_Name(String categoryName);
    
    List<Product> findByRestaurant_Id(UUID restaurantId);

    List<Product> findByRestaurant_IdAndName(UUID restaurantId, String name);

    List<Product> findByRestaurant_IdAndCategory_Name(UUID restaurantId, String categoryName);

    boolean existsByNameAndCategory_Name(String name, String categoryName);
}
