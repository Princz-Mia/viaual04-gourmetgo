package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.RestaurantCategory;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantCategoryDto;

import java.util.List;

public interface IRestaurantCategoryService {
    List<RestaurantCategoryDto> getAllCategories();

    RestaurantCategoryDto createCategory(String name);

    RestaurantCategoryDto convertRestaurantCategoryToDto(RestaurantCategory category);
}