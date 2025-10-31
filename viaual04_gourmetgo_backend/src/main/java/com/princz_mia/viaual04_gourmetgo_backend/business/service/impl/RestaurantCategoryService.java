package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IRestaurantCategoryService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.RestaurantCategory;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.RestaurantCategoryRepository;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantCategoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantCategoryService implements IRestaurantCategoryService {

    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<RestaurantCategoryDto> getAllCategories() {
        LoggingUtils.logMethodEntry(log, "getAllCategories");
        List<RestaurantCategoryDto> categories = restaurantCategoryRepository.findAll().stream()
                .map(this::convertRestaurantCategoryToDto)
                .collect(Collectors.toList());
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_CATEGORIES_RETRIEVED", "count", categories.size());
        return categories;
    }

    @Override
    public RestaurantCategoryDto createCategory(String name) {
        LoggingUtils.logMethodEntry(log, "createCategory", "name", name);
        RestaurantCategory restaurantCategory = restaurantCategoryRepository.findByName(name).orElse(null);
        if (restaurantCategory != null) {
            throw new AlreadyExistsException("Restaurant Category is already exists");
        }

        restaurantCategory = new RestaurantCategory();
        restaurantCategory.setName(name);
        restaurantCategory = restaurantCategoryRepository.save(restaurantCategory);
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_CATEGORY_CREATED", "categoryId", restaurantCategory.getId(), "name", name);
        return convertRestaurantCategoryToDto(restaurantCategory);
    }

    @Override
    public RestaurantCategoryDto convertRestaurantCategoryToDto(RestaurantCategory category) {
        LoggingUtils.logMethodEntry(log, "convertRestaurantCategoryToDto", "categoryId", category.getId());
        return modelMapper.map(category, RestaurantCategoryDto.class);
    }
}