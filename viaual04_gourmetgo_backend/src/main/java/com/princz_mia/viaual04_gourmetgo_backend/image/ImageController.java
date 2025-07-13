package com.princz_mia.viaual04_gourmetgo_backend.image;

import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.response.ApiResponse;
import lombok.RequiredArgsConstructor;
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
public class ImageController {

    private final IImageService imageService;

    @PostMapping("/products/upload")
    public ResponseEntity<ApiResponse> uploadProductImage(
            @RequestParam MultipartFile file,
            @RequestParam UUID productId
    ) {
        try {
            Image img = imageService.saveProductImage(file, productId);
            ImageDto imageDto = imageService.convertImageToDto(img);
            return ResponseEntity.ok(new ApiResponse("Upload success!", imageDto));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Upload failed!", ex.getMessage()));
        }
    }

    @PostMapping("/restaurants/upload")
    public ResponseEntity<ApiResponse> uploadRestaurantImage(
            @RequestParam MultipartFile file,
            @RequestParam UUID restaurantId
    ) {
        try {
            ImageDto imageDto = imageService.saveRestaurantImage(file, restaurantId);
            //ImageDto imageDto = imageService.convertImageToDto(img);
            return ResponseEntity.ok(new ApiResponse("Upload success!", imageDto));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Upload failed!", ex.getMessage()));
        }
    }

    @GetMapping("/download/{imageId}")
    @Transactional
    public ResponseEntity<Resource> downloadImage(@PathVariable UUID imageId) throws SQLException {
        Image image = imageService.getImageById(imageId);
        ByteArrayResource resource = new ByteArrayResource(image.getData().getBytes(1, (int) image.getData().length()));
        return  ResponseEntity.ok().contentType(MediaType.parseMediaType(image.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +image.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ApiResponse> getImageInfo(@PathVariable UUID imageId) {
        try {
            Image img = imageService.getImageById(imageId);
            ImageDto imageDto = imageService.convertImageToDto(img);
            return ResponseEntity.ok(new ApiResponse("Image retrieved", imageDto));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve image", ex.getMessage()));
        }
    }

    @PutMapping("/{imageId}")
    public ResponseEntity<ApiResponse> updateImage(
            @PathVariable UUID imageId,
            @RequestParam MultipartFile file
    ) {
        try {
            Image img = imageService.updateImage(file, imageId);
            ImageDto imageDto = imageService.convertImageToDto(img);
            return ResponseEntity.ok(new ApiResponse("Update success!", imageDto));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Update failed!", ex.getMessage()));
        }
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse> deleteImage(@PathVariable UUID imageId) {
        try {
            imageService.deleteImageById(imageId);
            return ResponseEntity.ok(new ApiResponse("Delete success!", null));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Delete failed!", ex.getMessage()));
        }
    }
}