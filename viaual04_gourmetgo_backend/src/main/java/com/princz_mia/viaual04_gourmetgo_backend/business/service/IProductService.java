package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Product;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ProductDto;

import java.util.List;
import java.util.UUID;

public interface IProductService {

    Product addProduct(ProductDto dto);

    Product getProductById(UUID id);

    void deleteProductById(UUID id);

    Product updateProduct(ProductDto dto, UUID id);

    List<Product> getAllProducts();

    List<Product> getProductByName(String name);

    List<Product> getProductsByCategoryName(String categoryName);

    List<ProductDto> getProductsByRestaurantId(UUID restaurantId);

    List<Product> getProductsByRestaurantIdAndName(UUID restaurantId, String name);

    List<Product> getProductsByRestaurantIdAndCategoryName(UUID restaurantId, String categoryName);

    List<ProductDto> getConvertedProducts(List<Product> products);

    ProductDto convertToDto(Product product);
}