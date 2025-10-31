package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IProductCategoryService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.ProductCategory;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.ProductCategoryRepository;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCategoryService implements IProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;

    @Override
    public ProductCategory getProductCategoryById(UUID id) {
        LoggingUtils.logMethodEntry(log, "getProductCategoryById", "id", id);
        return productCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product Category not found"));
    }

    @Override
    public ProductCategory getProductCategoryByName(String name) {
        LoggingUtils.logMethodEntry(log, "getProductCategoryByName", "name", name);
        return productCategoryRepository.findByName(name);
    }

    @Override
    public List<ProductCategory> getAllProductCategory() {
        LoggingUtils.logMethodEntry(log, "getAllProductCategory");
        List<ProductCategory> categories = productCategoryRepository.findAll();
        LoggingUtils.logBusinessEvent(log, "PRODUCT_CATEGORIES_RETRIEVED", "count", categories.size());
        return categories;
    }

    @Override
    public ProductCategory addProductCategory(ProductCategory productCategory) {
        LoggingUtils.logMethodEntry(log, "addProductCategory", "name", productCategory.getName());
        ProductCategory savedCategory = Optional.of(productCategory)
                .filter(c -> !productCategoryRepository.existsByName(c.getName()))
                .map(productCategoryRepository::save)
                .orElseThrow(() -> new AlreadyExistsException("Product Category already exists"));
        LoggingUtils.logBusinessEvent(log, "PRODUCT_CATEGORY_CREATED", "categoryId", savedCategory.getId(), "name", savedCategory.getName());
        return savedCategory;
    }

    @Override
    public ProductCategory updateProductCategory(ProductCategory productCategory, UUID id) {
        LoggingUtils.logMethodEntry(log, "updateProductCategory", "id", id, "name", productCategory.getName());
        ProductCategory updatedCategory = Optional.ofNullable(getProductCategoryById(id))
                .map(oldProductCategory -> {
                    oldProductCategory.setName(productCategory.getName());
                    return productCategoryRepository.save(oldProductCategory);
                }).orElseThrow(() -> new ResourceNotFoundException("Product Category not found"));
        LoggingUtils.logBusinessEvent(log, "PRODUCT_CATEGORY_UPDATED", "categoryId", id, "name", updatedCategory.getName());
        return updatedCategory;
    }

    @Override
    public void deleteProductCategory(UUID id) {
        LoggingUtils.logMethodEntry(log, "deleteProductCategory", "id", id);
        productCategoryRepository.findById(id)
                .ifPresentOrElse(category -> {
                    productCategoryRepository.delete(category);
                    LoggingUtils.logBusinessEvent(log, "PRODUCT_CATEGORY_DELETED", "categoryId", id, "name", category.getName());
                }, () ->{
                    throw new ResourceNotFoundException("Product Category not found");
                });
    }
}