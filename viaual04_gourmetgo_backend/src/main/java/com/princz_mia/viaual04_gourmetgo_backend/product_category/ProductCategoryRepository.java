package com.princz_mia.viaual04_gourmetgo_backend.product_category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {

    ProductCategory findByName(String name);

    boolean existsByName(String name);
}
