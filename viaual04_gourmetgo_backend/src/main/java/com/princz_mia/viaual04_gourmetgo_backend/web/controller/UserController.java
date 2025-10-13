package com.princz_mia.viaual04_gourmetgo_backend.user;

import com.princz_mia.viaual04_gourmetgo_backend.config.security.PasswordDto;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse("Success", users));
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<ApiResponse> lock(
            @PathVariable UUID id,
            @RequestBody LockRequest req
    ) {
        try {
            userService.lockUser(id, req.locked());
            return ResponseEntity.ok(new ApiResponse("Success", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(
            @RequestBody @Valid ProfileUpdateDto dto
    ) {
        try {
            User user = userService.getAuthenticatedCustomer();
            UserDto updated = userService.updateProfile(user, dto);
            return ResponseEntity.ok(new ApiResponse("Profile updated", updated));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (AppException ex) {
            return ResponseEntity.status(ex.getHttpStatus())
                    .body(new ApiResponse(ex.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable UUID id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse("Success", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @RequestParam("email")
            @NotBlank(message = "Email must not be blank")
            @Email(message = "Must be a valid email address")
            String email) {
        try {
            userService.requestPasswordReset(email);
            return ResponseEntity.ok(new ApiResponse("Reset link sent if email exists", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/reset-password/password")
    public ResponseEntity<ApiResponse> verifyUserAccount(
            @RequestParam("key") @NotEmpty(message = "Key cannot be empty or null") String key,
            @RequestBody @Valid PasswordDto passwordDto) {
        try {
            userService.resetPassword(key, passwordDto);
            return ResponseEntity.ok().body(new ApiResponse("Password reset was successful", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    // DTO for lock request
    public static record LockRequest(boolean locked) {}
}