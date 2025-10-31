package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IRestaurantCategoryService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantCategoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/restaurant-categories")
@RequiredArgsConstructor
@Slf4j
public class RestaurantCategoryController {

    private final IRestaurantCategoryService restaurantCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        LoggingUtils.logMethodEntry(log, "getAll");
        List<RestaurantCategoryDto> categories = restaurantCategoryService.getAllCategories();
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_CATEGORIES_RETRIEVED", "count", categories.size());
        return ResponseEntity.ok(new ApiResponse("Success", categories));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody RestaurantCategoryDto req) {
        LoggingUtils.logMethodEntry(log, "create", "name", req.getName());
        try {
            RestaurantCategoryDto category = restaurantCategoryService.createCategory(req.getName());
            LoggingUtils.logBusinessEvent(log, "RESTAURANT_CATEGORY_CREATED", "categoryId", category.getId(), "name", category.getName());
            return ResponseEntity.status(201).body(new ApiResponse("Created", category));
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to create restaurant category", e, "name", req.getName());
            throw new RuntimeException(e);
        }
    }
}
