package com.princz_mia.viaual04_gourmetgo_backend.restaurant;

import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final IRestaurantService restaurantService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable UUID id) {
        try {
            Restaurant restaurant = restaurantService.getRestaurantById(id);
            RestaurantDto restaurantDto = restaurantService.convertRestaurantToDto(restaurant);
            return ResponseEntity.ok(new ApiResponse("Success", restaurantDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
}
