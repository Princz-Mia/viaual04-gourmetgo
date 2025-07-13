package com.princz_mia.viaual04_gourmetgo_backend.product_category;

import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductCategoryService implements IProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    @Override
    public ProductCategory getProductCategoryById(UUID id) {
        return productCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product Category not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public ProductCategory getProductCategoryByName(String name) {
        return productCategoryRepository.findByName(name);
    }

    @Override
    public List<ProductCategory> getAllProductCategory() {
        return productCategoryRepository.findAll();
    }

    @Override
    public ProductCategory addProductCategory(ProductCategory productCategory) {
        return Optional.of(productCategory)
                .filter(c -> !productCategoryRepository.existsByName(c.getName()))
                .map(productCategoryRepository::save)
                .orElseThrow(() -> new AlreadyExistsException("Product Category already exists", HttpStatus.CONFLICT));
    }

    @Override
    public ProductCategory updateProductCategory(ProductCategory productCategory, UUID id) {
        return Optional.ofNullable(getProductCategoryById(id))
                .map(oldProductCategory -> {
                    oldProductCategory.setName(productCategory.getName());
                    return productCategoryRepository.save(oldProductCategory);
                }).orElseThrow(() -> new ResourceNotFoundException("Product Category not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public void deleteProductCategory(UUID id) {
        productCategoryRepository.findById(id)
                .ifPresentOrElse(productCategoryRepository::delete, () ->{
                    throw new ResourceNotFoundException("Product Category not found", HttpStatus.NOT_FOUND);
                });
    }
}
