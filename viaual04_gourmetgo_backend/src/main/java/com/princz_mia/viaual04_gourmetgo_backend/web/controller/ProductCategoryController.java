package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IProductCategoryService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.ProductCategory;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/products/categories")
@Slf4j
public class ProductCategoryController {

    private final IProductCategoryService productCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllProductCategories() {
        LoggingUtils.logMethodEntry(log, "getAllProductCategories");
        try {
            List<ProductCategory> productCategories = productCategoryService.getAllProductCategory();
            LoggingUtils.logBusinessEvent(log, "PRODUCT_CATEGORIES_RETRIEVED", "count", productCategories.size());
            return ResponseEntity.ok(new ApiResponse("Found!", productCategories));
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to retrieve product categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProductCategory(@RequestBody ProductCategory productCategory) {
        LoggingUtils.logMethodEntry(log, "addProductCategory", "name", productCategory.getName());
        try {
            ProductCategory savedProductCategory = productCategoryService.addProductCategory(productCategory);
            LoggingUtils.logBusinessEvent(log, "PRODUCT_CATEGORY_CREATED", "categoryId", savedProductCategory.getId(), "name", savedProductCategory.getName());
            return ResponseEntity.ok(new ApiResponse("Success!", savedProductCategory));
        } catch (AlreadyExistsException e) {
            LoggingUtils.logError(log, "Product category already exists", e, "name", productCategory.getName());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductCategoryById(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "getProductCategoryById", "id", id);
        try {
            ProductCategory productCategory = productCategoryService.getProductCategoryById(id);
            return ResponseEntity.ok(new ApiResponse("Found!", productCategory));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Product category not found", e, "id", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<ApiResponse> getProductCategoryByName(@PathVariable String name) {
        LoggingUtils.logMethodEntry(log, "getProductCategoryByName", "name", name);
        try {
            ProductCategory productCategory = productCategoryService.getProductCategoryByName(name);
            return ResponseEntity.ok(new ApiResponse("Found!", productCategory));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Product category not found by name", e, "name", name);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProductCategory(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "deleteProductCategory", "id", id);
        try {
            productCategoryService.deleteProductCategory(id);
            LoggingUtils.logBusinessEvent(log, "PRODUCT_CATEGORY_DELETED", "categoryId", id);
            return ResponseEntity.ok(new ApiResponse("Delete success!", null));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Product category not found for deletion", e, "id", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
}
