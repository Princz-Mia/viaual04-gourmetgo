package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Image;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ImageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface IImageService {
    Image getImageById(UUID id);
    void deleteImageById(UUID id);
    Image saveProductImage(MultipartFile file, UUID productId);
    ImageDto saveRestaurantImage(MultipartFile file, UUID restaurantId);
    Image updateImage(MultipartFile file, UUID id);
    ImageDto convertImageToDto(Image image);
}