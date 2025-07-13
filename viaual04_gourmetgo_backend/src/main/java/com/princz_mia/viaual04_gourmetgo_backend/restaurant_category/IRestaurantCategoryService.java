package com.princz_mia.viaual04_gourmetgo_backend.restaurant_category;

import com.princz_mia.viaual04_gourmetgo_backend.restaurant.Restaurant;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant.RestaurantDto;

import java.util.List;

public interface IRestaurantCategoryService {
    List<RestaurantCategoryDto> getAllCategories();

    RestaurantCategoryDto createCategory(String name);

    RestaurantCategoryDto convertRestaurantCategoryToDto(RestaurantCategory category);
}
