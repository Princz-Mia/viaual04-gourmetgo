package com.princz_mia.viaual04_gourmetgo_backend.product;

import com.princz_mia.viaual04_gourmetgo_backend.product_category.ProductCategory;
import com.princz_mia.viaual04_gourmetgo_backend.image.ImageDto;
import com.princz_mia.viaual04_gourmetgo_backend.product_category.ProductCategoryDto;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant.Restaurant;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant.RestaurantDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private int inventory;
    private ProductCategoryDto category;
    private RestaurantDto restaurant;
    private ImageDto image;
}
