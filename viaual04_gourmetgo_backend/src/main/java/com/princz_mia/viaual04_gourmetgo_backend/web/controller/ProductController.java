package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IProductService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Product;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ProductDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/products")
@Slf4j
public class ProductController {

    private final IProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllProducts() {
        LoggingUtils.logMethodEntry(log, "getAllProducts");
        long startTime = System.currentTimeMillis();
        
        List<Product> products = productService.getAllProducts();
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        
        LoggingUtils.logBusinessEvent(log, "PRODUCTS_RETRIEVED", "count", productDtos.size());
        LoggingUtils.logPerformance(log, "getAllProducts", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", productDtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "getProductById", "id", id);
        long startTime = System.currentTimeMillis();
        
        Product product = productService.getProductById(id);
        ProductDto productDto = productService.convertToDto(product);
        
        LoggingUtils.logBusinessEvent(log, "PRODUCT_RETRIEVED", "productId", id);
        LoggingUtils.logPerformance(log, "getProductById", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Product retrieved successfully", productDto));
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<ApiResponse> getProductsByName(@PathVariable String name) {
        LoggingUtils.logMethodEntry(log, "getProductsByName", "name", name);
        long startTime = System.currentTimeMillis();
        
        List<Product> products = productService.getProductByName(name);
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        
        LoggingUtils.logBusinessEvent(log, "PRODUCTS_BY_NAME_RETRIEVED", "name", name, "count", productDtos.size());
        LoggingUtils.logPerformance(log, "getProductsByName", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", productDtos));
    }

    @GetMapping("/by-category/{categoryName}")
    public ResponseEntity<ApiResponse> getProductsByCategoryName(@PathVariable String categoryName) {
        LoggingUtils.logMethodEntry(log, "getProductsByCategoryName", "categoryName", categoryName);
        long startTime = System.currentTimeMillis();
        
        List<Product> products = productService.getProductsByCategoryName(categoryName);
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        
        LoggingUtils.logBusinessEvent(log, "PRODUCTS_BY_CATEGORY_RETRIEVED", "categoryName", categoryName, "count", productDtos.size());
        LoggingUtils.logPerformance(log, "getProductsByCategoryName", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", productDtos));
    }

    @GetMapping("/by-restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse> getProductsByRestaurantId(@PathVariable UUID restaurantId) {
        LoggingUtils.logMethodEntry(log, "getProductsByRestaurantId", "restaurantId", restaurantId);
        long startTime = System.currentTimeMillis();
        
        List<ProductDto> products = productService.getProductsByRestaurantId(restaurantId);
        
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_PRODUCTS_RETRIEVED", "restaurantId", restaurantId, "count", products.size());
        LoggingUtils.logPerformance(log, "getProductsByRestaurantId", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", products));
    }

    @GetMapping("/by-restaurant-and-name")
    public ResponseEntity<ApiResponse> getProductsByRestaurantIdAndName(
            @RequestParam UUID restaurantId,
            @RequestParam String name) {
        LoggingUtils.logMethodEntry(log, "getProductsByRestaurantIdAndName", "restaurantId", restaurantId, "name", name);
        long startTime = System.currentTimeMillis();
        
        List<Product> products = productService.getProductsByRestaurantIdAndName(restaurantId, name);
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_PRODUCTS_BY_NAME_RETRIEVED", "restaurantId", restaurantId, "name", name, "count", productDtos.size());
        LoggingUtils.logPerformance(log, "getProductsByRestaurantIdAndName", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", productDtos));
    }

    @GetMapping("/by-restaurant-and-category")
    public ResponseEntity<ApiResponse> getProductsByRestaurantIdAndCategoryName(
            @RequestParam UUID restaurantId,
            @RequestParam String categoryName) {
        LoggingUtils.logMethodEntry(log, "getProductsByRestaurantIdAndCategoryName", "restaurantId", restaurantId, "categoryName", categoryName);
        long startTime = System.currentTimeMillis();
        
        List<Product> products = productService.getProductsByRestaurantIdAndCategoryName(restaurantId, categoryName);
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_PRODUCTS_BY_CATEGORY_RETRIEVED", "restaurantId", restaurantId, "categoryName", categoryName, "count", productDtos.size());
        LoggingUtils.logPerformance(log, "getProductsByRestaurantIdAndCategoryName", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", productDtos));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addProduct(@Valid @RequestBody ProductDto dto) {
        LoggingUtils.logMethodEntry(log, "addProduct", "name", dto.getName(), "restaurantId", dto.getRestaurant().getId());
        long startTime = System.currentTimeMillis();
        
        Product savedProduct = productService.addProduct(dto);
        ProductDto productDto = productService.convertToDto(savedProduct);
        
        LoggingUtils.logBusinessEvent(log, "PRODUCT_CREATED", "productId", savedProduct.getId(), "name", savedProduct.getName());
        LoggingUtils.logPerformance(log, "addProduct", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Product created successfully", productDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateProduct(@Valid @RequestBody ProductDto dto, @PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "updateProduct", "id", id, "name", dto.getName());
        long startTime = System.currentTimeMillis();
        
        Product product = productService.updateProduct(dto, id);
        ProductDto productDto = productService.convertToDto(product);
        
        LoggingUtils.logBusinessEvent(log, "PRODUCT_UPDATED", "productId", id, "name", product.getName());
        LoggingUtils.logPerformance(log, "updateProduct", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Product updated successfully", productDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "deleteProduct", "id", id);
        long startTime = System.currentTimeMillis();
        
        productService.deleteProductById(id);
        
        LoggingUtils.logBusinessEvent(log, "PRODUCT_DELETED", "productId", id);
        LoggingUtils.logPerformance(log, "deleteProduct", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Product deleted successfully", null));
    }
}