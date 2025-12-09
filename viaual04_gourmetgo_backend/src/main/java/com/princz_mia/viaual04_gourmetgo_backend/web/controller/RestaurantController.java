package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IRestaurantService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Restaurant;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.PasswordDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantRegistrationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/restaurants")
@RequiredArgsConstructor
@Slf4j
public class RestaurantController {
    private final IRestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllRestaurants() {
        LoggingUtils.logMethodEntry(log, "getAllRestaurants");
        long startTime = System.currentTimeMillis();
        
        List<RestaurantDto> list = restaurantService.getAllRestaurants();
        
        LoggingUtils.logBusinessEvent(log, "RESTAURANTS_RETRIEVED", "count", list.size());
        LoggingUtils.logPerformance(log, "getAllRestaurants", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Success", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "getById", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            Restaurant restaurant = restaurantService.getRestaurantById(id);
            RestaurantDto restaurantDto = restaurantService.convertRestaurantToDto(restaurant);
            
            LoggingUtils.logBusinessEvent(log, "RESTAURANT_RETRIEVED", "restaurantId", id);
            LoggingUtils.logPerformance(log, "getById", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Success", restaurantDto));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Restaurant not found", e, "id", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> register(
            @RequestPart("data") @Valid RestaurantRegistrationDto data,
            @RequestPart("logo") MultipartFile logo
    ) {
        LoggingUtils.logMethodEntry(log, "register", "name", data.getName(), "email", LoggingUtils.maskSensitiveData(data.getEmailAddress()));
        long startTime = System.currentTimeMillis();
        
        try {
            RestaurantDto dto = restaurantService.registerRestaurant(data, logo);
            
            LoggingUtils.logBusinessEvent(log, "RESTAURANT_REGISTERED", "restaurantId", dto.getId(), "name", dto.getName());
            LoggingUtils.logPerformance(log, "register", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("Restaurant registered", dto));
        } catch (Exception ex) {
            LoggingUtils.logError(log, "Restaurant registration failed", ex, "name", data.getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Registration failed", ex.getMessage()));
        }
    }

    @PostMapping("/verify/account")
    public ResponseEntity<ApiResponse> verifyUserAccount(
            @RequestParam("key") @NotEmpty(message = "Key cannot be empty or null") String key,
            @RequestBody @Valid PasswordDto passwordDto) {
        LoggingUtils.logMethodEntry(log, "verifyUserAccount", "key", LoggingUtils.maskSensitiveData(key));
        long startTime = System.currentTimeMillis();
        
        try {
            restaurantService.verifyAccountKey(key, passwordDto);
            
            LoggingUtils.logBusinessEvent(log, "RESTAURANT_ACCOUNT_VERIFIED", "key", LoggingUtils.maskSensitiveData(key));
            LoggingUtils.logPerformance(log, "verifyUserAccount", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok().body(new ApiResponse("Account verified successfully", HttpStatus.OK));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Account verification failed", e, "key", LoggingUtils.maskSensitiveData(key));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getPending() {
        LoggingUtils.logMethodEntry(log, "getPending");
        long startTime = System.currentTimeMillis();
        
        List<RestaurantDto> list = restaurantService.getPendingRestaurants();
        
        LoggingUtils.logBusinessEvent(log, "PENDING_RESTAURANTS_RETRIEVED", "count", list.size());
        LoggingUtils.logPerformance(log, "getPending", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Success", list));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> approve(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "approve", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            restaurantService.approveRestaurant(id);
            
            LoggingUtils.logBusinessEvent(log, "RESTAURANT_APPROVED", "restaurantId", id);
            LoggingUtils.logPerformance(log, "approve", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Success", null));
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to approve restaurant", e, "id", id);
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> reject(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "reject", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            restaurantService.rejectRestaurant(id);
            
            LoggingUtils.logBusinessEvent(log, "RESTAURANT_REJECTED", "restaurantId", id);
            LoggingUtils.logPerformance(log, "reject", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Success", null));
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to reject restaurant", e, "id", id);
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT') and @restaurantAccessControl.canAccess(authentication, #id) or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateRestaurant(
            @PathVariable UUID id,
            @RequestBody @Valid RestaurantDto dto
    ) {
        LoggingUtils.logMethodEntry(log, "updateRestaurant", "id", id, "name", dto.getName());
        long startTime = System.currentTimeMillis();
        
        try {
            RestaurantDto updated = restaurantService.updateRestaurant(id, dto);
            
            LoggingUtils.logBusinessEvent(log, "RESTAURANT_UPDATED", "restaurantId", id, "name", updated.getName());
            LoggingUtils.logPerformance(log, "updateRestaurant", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Restaurant updated", updated));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Restaurant not found for update", e, "id", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (AppException e) {
            LoggingUtils.logError(log, "Failed to update restaurant", e, "id", id);
            return ResponseEntity.status(e.getErrorType().getHttpStatus()).body(new ApiResponse(e.getMessage(), null));
        }
    }
}