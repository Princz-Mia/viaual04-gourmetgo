package com.princz_mia.viaual04_gourmetgo_backend.restaurant_category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantCategoryDto {
    private UUID id;
    private String name;
}
