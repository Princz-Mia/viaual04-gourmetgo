package com.princz_mia.viaual04_gourmetgo_backend.product;

import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.product_category.ProductCategory;
import com.princz_mia.viaual04_gourmetgo_backend.product_category.ProductCategoryRepository;
import com.princz_mia.viaual04_gourmetgo_backend.image.Image;
import com.princz_mia.viaual04_gourmetgo_backend.image.ImageDto;
import com.princz_mia.viaual04_gourmetgo_backend.image.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ImageRepository imageRepository;

    private final ModelMapper modelMapper;

    @Override
    public Product addProduct(ProductDto dto) {
        if (productExists(dto.getName(), dto.getCategory().getName())) {
            throw new AlreadyExistsException("Product already exists!", HttpStatus.CONFLICT);
        }

        ProductCategory productCategory = Optional.ofNullable(productCategoryRepository.findByName(dto.getCategory().getName()))
                .orElseGet(() -> {
                    ProductCategory newProductCategory = ProductCategory.builder()
                            .name(dto.getCategory().getName())
                            .build();
                    return productCategoryRepository.save(newProductCategory);
                });

        return productRepository.save(createProduct(dto, productCategory));
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
        return productRepository.findById(id)
                .orElseThrow(() -> new AppException("Product not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public void deleteProductById(UUID id) {
        productRepository.findById(id)
                .ifPresentOrElse(productRepository::delete,
                        () -> { throw new AppException("Product not found", HttpStatus.NOT_FOUND); });
    }

    @Override
    public Product updateProduct(ProductDto dto, UUID productId) {
        return productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct, dto))
                .map(productRepository::save)
                .orElseThrow(() -> new AppException("Product not found", HttpStatus.NOT_FOUND));
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
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public List<Product> getProductsByCategoryName(String categoryName) {
        return productRepository.findByCategory_Name(categoryName);
    }

    @Override
    public List<ProductDto> getProductsByRestaurantId(UUID restaurantId) {
        return productRepository.findByRestaurant_Id(restaurantId).stream()
                .filter(product -> !product.isDeleted())
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> getProductsByRestaurantIdAndName(UUID restaurantId, String name) {
        return productRepository.findByRestaurant_IdAndName(restaurantId, name);
    }

    @Override
    public List<Product> getProductsByRestaurantIdAndCategoryName(UUID restaurantId, String categoryName) {
        return productRepository.findByRestaurant_IdAndCategory_Name(restaurantId, categoryName);
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public ProductDto convertToDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);

        Image productImage = imageRepository.findByProduct_Id(product.getId());
        if (productImage != null) {
            ImageDto imageDto = modelMapper.map(productImage, ImageDto.class);
            productDto.setImage(imageDto);
        }

        return productDto;
    }
}
