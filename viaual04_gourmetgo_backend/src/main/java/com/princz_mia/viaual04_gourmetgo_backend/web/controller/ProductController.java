package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IProductService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Product;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ProductDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/products")
public class ProductController {

    private final IProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", productDtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable UUID id) {
        Product product = productService.getProductById(id);
        ProductDto productDto = productService.convertToDto(product);
        return ResponseEntity.ok(new ApiResponse("Product retrieved successfully", productDto));
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<ApiResponse> getProductsByName(@PathVariable String name) {
        List<Product> products = productService.getProductByName(name);
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", productDtos));
    }

    @GetMapping("/by-category/{categoryName}")
    public ResponseEntity<ApiResponse> getProductsByCategoryName(@PathVariable String categoryName) {
        List<Product> products = productService.getProductsByCategoryName(categoryName);
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", productDtos));
    }

    @GetMapping("/by-restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse> getProductsByRestaurantId(@PathVariable UUID restaurantId) {
        List<ProductDto> products = productService.getProductsByRestaurantId(restaurantId);
        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", products));
    }

    @GetMapping("/by-restaurant-and-name")
    public ResponseEntity<ApiResponse> getProductsByRestaurantIdAndName(
            @RequestParam UUID restaurantId,
            @RequestParam String name) {
        List<Product> products = productService.getProductsByRestaurantIdAndName(restaurantId, name);
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", productDtos));
    }

    @GetMapping("/by-restaurant-and-category")
    public ResponseEntity<ApiResponse> getProductsByRestaurantIdAndCategoryName(
            @RequestParam UUID restaurantId,
            @RequestParam String categoryName) {
        List<Product> products = productService.getProductsByRestaurantIdAndCategoryName(restaurantId, categoryName);
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", productDtos));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addProduct(@Valid @RequestBody ProductDto dto) {
        Product savedProduct = productService.addProduct(dto);
        ProductDto productDto = productService.convertToDto(savedProduct);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Product created successfully", productDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateProduct(@Valid @RequestBody ProductDto dto, @PathVariable UUID id) {
        Product product = productService.updateProduct(dto, id);
        ProductDto productDto = productService.convertToDto(product);
        return ResponseEntity.ok(new ApiResponse("Product updated successfully", productDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable UUID id) {
        productService.deleteProductById(id);
        return ResponseEntity.ok(new ApiResponse("Product deleted successfully", null));
    }
}
