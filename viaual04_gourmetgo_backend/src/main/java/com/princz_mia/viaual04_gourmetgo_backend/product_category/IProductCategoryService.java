package com.princz_mia.viaual04_gourmetgo_backend.product_category;

import java.util.List;
import java.util.UUID;

public interface IProductCategoryService {

    ProductCategory getProductCategoryById(UUID id)
            ;
    ProductCategory getProductCategoryByName(String name);

    List<ProductCategory> getAllProductCategory();

    ProductCategory addProductCategory(ProductCategory productCategory);

    ProductCategory updateProductCategory(ProductCategory productCategory, UUID id);

    void deleteProductCategory(UUID id);

}
