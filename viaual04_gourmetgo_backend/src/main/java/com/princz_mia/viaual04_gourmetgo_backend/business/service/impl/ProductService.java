package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IProductService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Image;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Product;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.ProductCategory;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.ImageRepository;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.ProductCategoryRepository;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.ProductRepository;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ErrorType;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ImageDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Service;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService implements IProductService
{

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ImageRepository imageRepository;

    private final ModelMapper modelMapper;

    @Override
    public Product addProduct(ProductDto dto) {
        LoggingUtils.logMethodEntry(log, "addProduct", "name", dto.getName(), "category", dto.getCategory().getName());
        if (productExists(dto.getName(), dto.getCategory().getName())) {
            throw new AlreadyExistsException("Product already exists!");
        }

        ProductCategory productCategory = Optional.ofNullable(productCategoryRepository.findByName(dto.getCategory().getName()))
                .orElseGet(() -> {
                    ProductCategory newProductCategory = ProductCategory.builder()
                            .name(dto.getCategory().getName())
                            .build();
                    return productCategoryRepository.save(newProductCategory);
                });

        Product savedProduct = productRepository.save(createProduct(dto, productCategory));
        LoggingUtils.logBusinessEvent(log, "PRODUCT_CREATED", "productId", savedProduct.getId(), "name", savedProduct.getName());
        return savedProduct;
    }

    private boolean productExists(String name, String categoryName) {
        return productRepository.existsByNameAndCategory_Name(name, categoryName);
    }

    private Product createProduct(ProductDto dto, ProductCategory productCategory) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .inventory(dto.getInventory())
                .category(productCategory)
                .build();
    }

    @Override
    public Product getProductById(UUID id) {
        LoggingUtils.logMethodEntry(log, "getProductById", "id", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new AppException("Product not found", ErrorType.RESOURCE_NOT_FOUND));
    }

    @Override
    public void deleteProductById(UUID id) {
        LoggingUtils.logMethodEntry(log, "deleteProductById", "id", id);
        productRepository.findById(id)
                .ifPresentOrElse(product -> {
                    productRepository.delete(product);
                    LoggingUtils.logBusinessEvent(log, "PRODUCT_DELETED", "productId", id, "name", product.getName());
                }, () -> { throw new AppException("Product not found", ErrorType.RESOURCE_NOT_FOUND); });
    }

    @Override
    public Product updateProduct(ProductDto dto, UUID productId) {
        LoggingUtils.logMethodEntry(log, "updateProduct", "productId", productId, "name", dto.getName());
        Product updatedProduct = productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct, dto))
                .map(productRepository::save)
                .orElseThrow(() -> new AppException("Product not found", ErrorType.RESOURCE_NOT_FOUND));
        LoggingUtils.logBusinessEvent(log, "PRODUCT_UPDATED", "productId", productId, "name", updatedProduct.getName());
        return updatedProduct;
    }

    private Product updateExistingProduct(Product existingProduct, ProductDto dto) {
        existingProduct.setName(dto.getName());
        existingProduct.setDescription(dto.getDescription());
        existingProduct.setPrice(dto.getPrice());
        existingProduct.setInventory(dto.getInventory());

        ProductCategory productCategory = productCategoryRepository.findByName(dto.getCategory().getName());
        existingProduct.setCategory(productCategory);

        return existingProduct;
    }

    @Override
    public List<Product> getAllProducts() {
        LoggingUtils.logMethodEntry(log, "getAllProducts");
        List<Product> products = productRepository.findAll();
        LoggingUtils.logBusinessEvent(log, "PRODUCTS_RETRIEVED", "count", products.size());
        return products;
    }

    @Override
    public List<Product> getProductByName(String name) {
        LoggingUtils.logMethodEntry(log, "getProductByName", "name", name);
        List<Product> products = productRepository.findByName(name);
        LoggingUtils.logBusinessEvent(log, "PRODUCTS_BY_NAME_RETRIEVED", "name", name, "count", products.size());
        return products;
    }

    @Override
    public List<Product> getProductsByCategoryName(String categoryName) {
        LoggingUtils.logMethodEntry(log, "getProductsByCategoryName", "categoryName", categoryName);
        List<Product> products = productRepository.findByCategory_Name(categoryName);
        LoggingUtils.logBusinessEvent(log, "PRODUCTS_BY_CATEGORY_RETRIEVED", "categoryName", categoryName, "count", products.size());
        return products;
    }

    @Override
    public List<ProductDto> getProductsByRestaurantId(UUID restaurantId) {
        LoggingUtils.logMethodEntry(log, "getProductsByRestaurantId", "restaurantId", restaurantId);
        List<ProductDto> products = productRepository.findByRestaurant_Id(restaurantId).stream()
                .filter(product -> !product.isDeleted())
                .map(this::convertToDto)
                .collect(Collectors.toList());
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_PRODUCTS_RETRIEVED", "restaurantId", restaurantId, "count", products.size());
        return products;
    }

    @Override
    public List<Product> getProductsByRestaurantIdAndName(UUID restaurantId, String name) {
        LoggingUtils.logMethodEntry(log, "getProductsByRestaurantIdAndName", "restaurantId", restaurantId, "name", name);
        List<Product> products = productRepository.findByRestaurant_IdAndName(restaurantId, name);
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_PRODUCTS_BY_NAME_RETRIEVED", "restaurantId", restaurantId, "name", name, "count", products.size());
        return products;
    }

    @Override
    public List<Product> getProductsByRestaurantIdAndCategoryName(UUID restaurantId, String categoryName) {
        LoggingUtils.logMethodEntry(log, "getProductsByRestaurantIdAndCategoryName", "restaurantId", restaurantId, "categoryName", categoryName);
        List<Product> products = productRepository.findByRestaurant_IdAndCategory_Name(restaurantId, categoryName);
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_PRODUCTS_BY_CATEGORY_RETRIEVED", "restaurantId", restaurantId, "categoryName", categoryName, "count", products.size());
        return products;
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
        LoggingUtils.logMethodEntry(log, "getConvertedProducts", "count", products.size());
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public ProductDto convertToDto(Product product) {
        LoggingUtils.logMethodEntry(log, "convertToDto", "productId", product.getId());
        ProductDto productDto = modelMapper.map(product, ProductDto.class);

        Image productImage = imageRepository.findByProduct_Id(product.getId());
        if (productImage != null) {
            ImageDto imageDto = modelMapper.map(productImage, ImageDto.class);
            productDto.setImage(imageDto);
        }

        return productDto;
    }
}