package com.princz_mia.viaual04_gourmetgo_backend.restaurant_category;

import com.princz_mia.viaual04_gourmetgo_backend.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/restaurant-categories")
@RequiredArgsConstructor
public class RestaurantCategoryController {

    private final IRestaurantCategoryService restaurantCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        List<RestaurantCategoryDto> categories = restaurantCategoryService.getAllCategories();
        return ResponseEntity.ok(new ApiResponse("Success", categories));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody RestaurantCategoryDto req) {
        try {
            RestaurantCategoryDto category = restaurantCategoryService.createCategory(req.getName());
            return ResponseEntity.status(201).body(new ApiResponse("Created", category));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
