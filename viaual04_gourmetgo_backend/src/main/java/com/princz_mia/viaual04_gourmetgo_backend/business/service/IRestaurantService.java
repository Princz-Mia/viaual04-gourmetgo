package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.web.dto.PasswordDto;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Restaurant;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantRegistrationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IRestaurantService {
    Restaurant getRestaurantById(UUID id);

    List<RestaurantDto> getAllRestaurants();

    RestaurantDto registerRestaurant(RestaurantRegistrationDto reg, MultipartFile logo);

    RestaurantDto convertRestaurantToDto(Restaurant restaurant);

    List<RestaurantDto> getPendingRestaurants();

    void approveRestaurant(UUID id);

    void rejectRestaurant(UUID id);

    void verifyAccountKey(@NotEmpty(message = "Key cannot be empty or null") String key, PasswordDto passwordDto);

    RestaurantDto updateRestaurant(UUID id, @Valid RestaurantDto dto);
}