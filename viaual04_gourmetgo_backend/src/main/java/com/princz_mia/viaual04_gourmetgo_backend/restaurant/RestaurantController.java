package com.princz_mia.viaual04_gourmetgo_backend.restaurant;

import com.princz_mia.viaual04_gourmetgo_backend.config.security.PasswordDto;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final IRestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllRestaurants() {
        List<RestaurantDto> list = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(new ApiResponse("Success", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable UUID id) {
        try {
            Restaurant restaurant = restaurantService.getRestaurantById(id);
            RestaurantDto restaurantDto = restaurantService.convertRestaurantToDto(restaurant);
            return ResponseEntity.ok(new ApiResponse("Success", restaurantDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> register(
            @RequestPart("data") @Valid RestaurantRegistrationDto data,
            @RequestPart("logo") MultipartFile logo
    ) {
        try {
            RestaurantDto dto = restaurantService.registerRestaurant(data, logo);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("Restaurant registered", dto));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Registration failed", ex.getMessage()));
        }
    }

    @PostMapping("/verify/account")
    public ResponseEntity<ApiResponse> verifyUserAccount(
            @RequestParam("key") @NotEmpty(message = "Key cannot be empty or null") String key,
            @RequestBody @Valid PasswordDto passwordDto) {
        try {
            restaurantService.verifyAccountKey(key, passwordDto);
            return ResponseEntity.ok().body(new ApiResponse("Account verified successfully", HttpStatus.OK));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse> getPending() {
        List<RestaurantDto> list = restaurantService.getPendingRestaurants();
        return ResponseEntity.ok(new ApiResponse("Success", list));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse> approve(@PathVariable UUID id) {
        try {
            restaurantService.approveRestaurant(id);
            return ResponseEntity.ok(new ApiResponse("Success", null));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse> reject(@PathVariable UUID id) {
        try {
            restaurantService.rejectRestaurant(id);
            return ResponseEntity.ok(new ApiResponse("Success", null));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateRestaurant(
            @PathVariable UUID id,
            @RequestBody @Valid RestaurantDto dto
    ) {
        try {
            RestaurantDto updated = restaurantService.updateRestaurant(id, dto);
            return ResponseEntity.ok(new ApiResponse("Restaurant updated", updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (AppException e) {
            return ResponseEntity.status(e.getHttpStatus()).body(new ApiResponse(e.getMessage(), null));
        }
    }
}
