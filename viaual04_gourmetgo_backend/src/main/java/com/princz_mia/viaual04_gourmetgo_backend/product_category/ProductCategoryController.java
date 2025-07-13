package com.princz_mia.viaual04_gourmetgo_backend.product_category;

import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/products/categories")
public class ProductCategoryController {

    private final IProductCategoryService productCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllProductCategories() {
        try {
            List<ProductCategory> productCategories = productCategoryService.getAllProductCategory();
            return ResponseEntity.ok(new ApiResponse("Found!", productCategories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProductCategory(@RequestBody ProductCategory productCategory) {
        try {
            ProductCategory savedProductCategory = productCategoryService.addProductCategory(productCategory);
            return ResponseEntity.ok(new ApiResponse("Success!", savedProductCategory));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
        // unreachable
        //             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductCategoryById(@PathVariable UUID id) {
        try {
            ProductCategory productCategory = productCategoryService.getProductCategoryById(id);
            return ResponseEntity.ok(new ApiResponse("Found!", productCategory));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
        // unreachable
        //             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null))
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<ApiResponse> getProductCategoryByName(@PathVariable String name) {
        try {
            ProductCategory productCategory = productCategoryService.getProductCategoryByName(name);
            return ResponseEntity.ok(new ApiResponse("Found!", productCategory));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
        // unreachable
        //             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProductCategory(@PathVariable UUID id) {
        try {
            productCategoryService.deleteProductCategory(id);
            return ResponseEntity.ok(new ApiResponse("Delete success!", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
        // unreachable
        //             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
    }
}
