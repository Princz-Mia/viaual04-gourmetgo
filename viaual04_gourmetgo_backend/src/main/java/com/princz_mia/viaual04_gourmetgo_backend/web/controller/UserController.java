package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IUserService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.User;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.PasswordDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ProfileUpdateDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final IUserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        LoggingUtils.logMethodEntry(log, "getAll");
        List<UserDto> users = userService.getAllUsers();
        LoggingUtils.logBusinessEvent(log, "USERS_RETRIEVED", "count", users.size());
        return ResponseEntity.ok(new ApiResponse("Success", users));
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<ApiResponse> lock(
            @PathVariable UUID id,
            @RequestBody LockRequest req
    ) {
        LoggingUtils.logMethodEntry(log, "lock", "id", id, "locked", req.locked());
        try {
            userService.lockUser(id, req.locked());
            LoggingUtils.logBusinessEvent(log, "USER_LOCK_STATUS_CHANGED", "userId", id, "locked", req.locked());
            return ResponseEntity.ok(new ApiResponse("Success", null));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "User not found for lock operation", e, "id", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getProfile() {
        LoggingUtils.logMethodEntry(log, "getProfile");
        try {
            User user = userService.getAuthenticatedCustomer();
            UserDto userDto = userService.getUserById(user.getId());
            LoggingUtils.logBusinessEvent(log, "USER_PROFILE_RETRIEVED", "userId", user.getId());
            return ResponseEntity.ok(new ApiResponse("Profile retrieved", userDto));
        } catch (ResourceNotFoundException ex) {
            LoggingUtils.logError(log, "User not found for profile retrieval", ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage(), null));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(
            @RequestBody @Valid ProfileUpdateDto dto
    ) {
        LoggingUtils.logMethodEntry(log, "updateProfile", "email", LoggingUtils.maskSensitiveData(dto.getEmailAddress()));
        try {
            User user = userService.getAuthenticatedCustomer();
            UserDto updated = userService.updateProfile(user, dto);
            LoggingUtils.logBusinessEvent(log, "USER_PROFILE_UPDATED", "userId", user.getId());
            return ResponseEntity.ok(new ApiResponse("Profile updated", updated));
        } catch (ResourceNotFoundException ex) {
            LoggingUtils.logError(log, "User not found for profile update", ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(ex.getMessage(), null));
        } catch (AppException ex) {
            LoggingUtils.logError(log, "Failed to update user profile", ex);
            return ResponseEntity.status(ex.getErrorType().getHttpStatus())
                    .body(new ApiResponse(ex.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "delete", "id", id);
        try {
            userService.deleteUser(id);
            LoggingUtils.logBusinessEvent(log, "USER_DELETED", "userId", id);
            return ResponseEntity.ok(new ApiResponse("Success", null));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "User not found for deletion", e, "id", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @RequestParam("email")
            @NotBlank(message = "Email must not be blank")
            @Email(message = "Must be a valid email address")
            String email) {
        LoggingUtils.logMethodEntry(log, "forgotPassword", "email", LoggingUtils.maskSensitiveData(email));
        try {
            userService.requestPasswordReset(email);
            LoggingUtils.logBusinessEvent(log, "PASSWORD_RESET_REQUESTED", "email", LoggingUtils.maskSensitiveData(email));
            return ResponseEntity.ok(new ApiResponse("Reset link sent if email exists", null));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "User not found for password reset", e, "email", LoggingUtils.maskSensitiveData(email));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/reset-password/password")
    public ResponseEntity<ApiResponse> verifyUserAccount(
            @RequestParam("key") @NotEmpty(message = "Key cannot be empty or null") String key,
            @RequestBody @Valid PasswordDto passwordDto) {
        LoggingUtils.logMethodEntry(log, "verifyUserAccount", "key", LoggingUtils.maskSensitiveData(key));
        try {
            userService.resetPassword(key, passwordDto);
            LoggingUtils.logBusinessEvent(log, "PASSWORD_RESET_COMPLETED", "key", LoggingUtils.maskSensitiveData(key));
            return ResponseEntity.ok().body(new ApiResponse("Password reset was successful", null));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Password reset failed", e, "key", LoggingUtils.maskSensitiveData(key));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    // DTO for lock request
    public static record LockRequest(boolean locked) {}
}