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
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService implements IImageService
{
    private final ImageRepository imageRepository;
    private final IProductService productService;
    private final RestaurantRepository restaurantRepository;

    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Image getImageById(UUID id) {
        Image img = imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No image found", HttpStatus.NOT_FOUND));
        return img;
    }

    @Override
    public void deleteImageById(UUID id) {
        Image img = imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No image found", HttpStatus.NOT_FOUND));
        imageRepository.delete(img);
    }

    @Override
    @Transactional
    public Image saveProductImage(MultipartFile file, UUID productId) {
        Product product = productService.getProductById(productId);
        return saveImage(file, img -> img.setProduct(product));
    }

    @Override
    public ImageDto saveRestaurantImage(MultipartFile file, UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("No restaurant found", HttpStatus.NOT_FOUND));
        return convertImageToDto(saveImage(file, img -> img.setRestaurant(restaurant)));
    }

    @Override
    @Transactional
    public Image updateImage(MultipartFile file, UUID id) {
        Image img = imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No image found", HttpStatus.NOT_FOUND));
        try {
            img.setFileName(file.getOriginalFilename());
            img.setFileType(file.getContentType());
            img.setData(new SerialBlob(file.getBytes()));
            img = imageRepository.save(img);
            return img;
        } catch (IOException | SQLException e) {
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