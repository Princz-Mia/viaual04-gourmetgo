package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IImageService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Image;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ImageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLException;
import java.util.UUID;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("${api.prefix}/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final IImageService imageService;

    @PostMapping("/products/upload")
    public ResponseEntity<ApiResponse> uploadProductImage(
            @RequestParam MultipartFile file,
            @RequestParam UUID productId
    ) {
        LoggingUtils.logMethodEntry(log, "uploadProductImage", "productId", productId, "fileName", file.getOriginalFilename());
        long startTime = System.currentTimeMillis();
        
        try {
            Image img = imageService.saveProductImage(file, productId);
            ImageDto imageDto = imageService.convertImageToDto(img);
            
            LoggingUtils.logBusinessEvent(log, "PRODUCT_IMAGE_UPLOADED", "productId", productId, "imageId", img.getId());
            LoggingUtils.logPerformance(log, "uploadProductImage", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Upload success!", imageDto));
        } catch (ResourceNotFoundException ex) {
            LoggingUtils.logError(log, "Product not found for image upload", ex, "productId", productId);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception ex) {
            LoggingUtils.logError(log, "Failed to upload product image", ex, "productId", productId);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Upload failed!", ex.getMessage()));
        }
    }

    @PostMapping("/restaurants/upload")
    public ResponseEntity<ApiResponse> uploadRestaurantImage(
            @RequestParam MultipartFile file,
            @RequestParam UUID restaurantId
    ) {
        LoggingUtils.logMethodEntry(log, "uploadRestaurantImage", "restaurantId", restaurantId, "fileName", file.getOriginalFilename());
        long startTime = System.currentTimeMillis();
        
        try {
            ImageDto imageDto = imageService.saveRestaurantImage(file, restaurantId);
            
            LoggingUtils.logBusinessEvent(log, "RESTAURANT_IMAGE_UPLOADED", "restaurantId", restaurantId, "imageId", imageDto.getId());
            LoggingUtils.logPerformance(log, "uploadRestaurantImage", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Upload success!", imageDto));
        } catch (ResourceNotFoundException ex) {
            LoggingUtils.logError(log, "Restaurant not found for image upload", ex, "restaurantId", restaurantId);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception ex) {
            LoggingUtils.logError(log, "Failed to upload restaurant image", ex, "restaurantId", restaurantId);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Upload failed!", ex.getMessage()));
        }
    }

    @GetMapping("/download/{imageId}")
    @Transactional
    public ResponseEntity<Resource> downloadImage(@PathVariable UUID imageId) throws SQLException {
        LoggingUtils.logMethodEntry(log, "downloadImage", "imageId", imageId);
        long startTime = System.currentTimeMillis();
        
        Image image = imageService.getImageById(imageId);
        ByteArrayResource resource = new ByteArrayResource(image.getData().getBytes(1, (int) image.getData().length()));
        
        LoggingUtils.logBusinessEvent(log, "IMAGE_DOWNLOADED", "imageId", imageId, "fileName", image.getFileName());
        LoggingUtils.logPerformance(log, "downloadImage", System.currentTimeMillis() - startTime);
        
        return  ResponseEntity.ok().contentType(MediaType.parseMediaType(image.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +image.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ApiResponse> getImageInfo(@PathVariable UUID imageId) {
        LoggingUtils.logMethodEntry(log, "getImageInfo", "imageId", imageId);
        long startTime = System.currentTimeMillis();
        
        try {
            Image img = imageService.getImageById(imageId);
            ImageDto imageDto = imageService.convertImageToDto(img);
            
            LoggingUtils.logBusinessEvent(log, "IMAGE_INFO_RETRIEVED", "imageId", imageId);
            LoggingUtils.logPerformance(log, "getImageInfo", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Image retrieved", imageDto));
        } catch (ResourceNotFoundException ex) {
            LoggingUtils.logError(log, "Image not found", ex, "imageId", imageId);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception ex) {
            LoggingUtils.logError(log, "Failed to retrieve image info", ex, "imageId", imageId);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve image", ex.getMessage()));
        }
    }

    @PutMapping("/{imageId}")
    public ResponseEntity<ApiResponse> updateImage(
            @PathVariable UUID imageId,
            @RequestParam MultipartFile file
    ) {
        LoggingUtils.logMethodEntry(log, "updateImage", "imageId", imageId, "fileName", file.getOriginalFilename());
        long startTime = System.currentTimeMillis();
        
        try {
            Image img = imageService.updateImage(file, imageId);
            ImageDto imageDto = imageService.convertImageToDto(img);
            
            LoggingUtils.logBusinessEvent(log, "IMAGE_UPDATED", "imageId", imageId, "fileName", file.getOriginalFilename());
            LoggingUtils.logPerformance(log, "updateImage", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Update success!", imageDto));
        } catch (ResourceNotFoundException ex) {
            LoggingUtils.logError(log, "Image not found for update", ex, "imageId", imageId);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception ex) {
            LoggingUtils.logError(log, "Failed to update image", ex, "imageId", imageId);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Update failed!", ex.getMessage()));
        }
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse> deleteImage(@PathVariable UUID imageId) {
        LoggingUtils.logMethodEntry(log, "deleteImage", "imageId", imageId);
        long startTime = System.currentTimeMillis();
        
        try {
            imageService.deleteImageById(imageId);
            
            LoggingUtils.logBusinessEvent(log, "IMAGE_DELETED", "imageId", imageId);
            LoggingUtils.logPerformance(log, "deleteImage", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Delete success!", null));
        } catch (ResourceNotFoundException ex) {
            LoggingUtils.logError(log, "Image not found for deletion", ex, "imageId", imageId);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception ex) {
            LoggingUtils.logError(log, "Failed to delete image", ex, "imageId", imageId);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Delete failed!", ex.getMessage()));
        }
    }
}