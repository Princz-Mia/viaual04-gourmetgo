package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IRestaurantCategoryService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.RestaurantCategory;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.RestaurantCategoryRepository;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantCategoryDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantCategoryService implements IRestaurantCategoryService
{

    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<RestaurantCategoryDto> getAllCategories() {
        return restaurantCategoryRepository.findAll().stream()
                .map(this::convertRestaurantCategoryToDto)
                .collect(Collectors.toList());
    }

    @Override
    public RestaurantCategoryDto createCategory(String name) {
        RestaurantCategory restaurantCategory = restaurantCategoryRepository.findByName(name).orElse(null);
        if (restaurantCategory != null) {
            throw new AlreadyExistsException("Restaurant Category is already exists", HttpStatus.BAD_REQUEST);
        }

        restaurantCategory = new RestaurantCategory();
        restaurantCategory.setName(name);
        restaurantCategory = restaurantCategoryRepository.save(restaurantCategory);
        return convertRestaurantCategoryToDto(restaurantCategory);
    }

    @Override
    public RestaurantCategoryDto convertRestaurantCategoryToDto(RestaurantCategory category) {
        return modelMapper.map(category, RestaurantCategoryDto.class);
    }

}