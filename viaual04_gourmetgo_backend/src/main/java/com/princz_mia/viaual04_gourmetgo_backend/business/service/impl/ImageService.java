package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IImageService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.IProductService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Image;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Product;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Restaurant;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.ImageRepository;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.RestaurantRepository;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ImageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService implements IImageService {
    private final ImageRepository imageRepository;
    private final IProductService productService;
    private final RestaurantRepository restaurantRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Image getImageById(UUID id) {
        LoggingUtils.logMethodEntry(log, "getImageById", "id", id);
        Image img = imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No image found"));
        return img;
    }

    @Override
    public void deleteImageById(UUID id) {
        LoggingUtils.logMethodEntry(log, "deleteImageById", "id", id);
        Image img = imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No image found"));
        imageRepository.delete(img);
        LoggingUtils.logBusinessEvent(log, "IMAGE_DELETED", "imageId", id);
    }

    @Override
    @Transactional
    public Image saveProductImage(MultipartFile file, UUID productId) {
        LoggingUtils.logMethodEntry(log, "saveProductService", "productId", productId, "fileName", file.getOriginalFilename());
        Product product = productService.getProductById(productId);
        
        // Check if product already has an image and update it instead
        if (product.getImage() != null) {
            return updateImage(file, product.getImage().getId());
        }
        
        Image savedImage = saveImage(file, img -> img.setProduct(product));
        LoggingUtils.logBusinessEvent(log, "PRODUCT_IMAGE_SAVED", "imageId", savedImage.getId(), "productId", productId);
        return savedImage;
    }

    @Override
    @Transactional
    public ImageDto saveRestaurantImage(MultipartFile file, UUID restaurantId) {
        LoggingUtils.logMethodEntry(log, "saveRestaurantImage", "restaurantId", restaurantId, "fileName", file.getOriginalFilename());
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("No restaurant found"));
        
        // Check if restaurant already has an image and update it instead
        if (restaurant.getLogo() != null) {
            Image existingImage = restaurant.getLogo();
            return convertImageToDto(updateImage(file, existingImage.getId()));
        }
        
        Image savedImage = saveImage(file, img -> img.setRestaurant(restaurant));
        
        // Update restaurant's logo reference
        restaurant.setLogo(savedImage);
        restaurantRepository.save(restaurant);
        
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_IMAGE_SAVED", "imageId", savedImage.getId(), "restaurantId", restaurantId);
        return convertImageToDto(savedImage);
    }

    @Override
    @Transactional
    public Image updateImage(MultipartFile file, UUID id) {
        LoggingUtils.logMethodEntry(log, "updateImage", "id", id, "fileName", file.getOriginalFilename());
        Image img = imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No image found"));
        try {
            img.setFileName(file.getOriginalFilename());
            img.setFileType(file.getContentType());
            img.setData(new SerialBlob(file.getBytes()));
            img = imageRepository.save(img);
            LoggingUtils.logBusinessEvent(log, "IMAGE_UPDATED", "imageId", id, "fileName", file.getOriginalFilename());
            return img;
        } catch (IOException | SQLException e) {
            LoggingUtils.logError(log, "Failed to update image", e, "id", id);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Image saveImage(MultipartFile file, java.util.function.Consumer<Image> setOwner) {
        try {
            Image img = Image.builder()
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .data(new SerialBlob(file.getBytes()))
                    .build();
            setOwner.accept(img);
            img = imageRepository.save(img);

            img.setDownloadUrl("/api/v1/images/download/" + img.getId());

            imageRepository.save(img);
            return img;
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public ImageDto convertImageToDto(Image image) {
        return modelMapper.map(image, ImageDto.class);
    }
}