package com.princz_mia.viaual04_gourmetgo_backend.user;

import com.princz_mia.viaual04_gourmetgo_backend.config.security.PasswordDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    List<UserDto> getAllUsers();
    void lockUser(UUID userId, boolean locked);
    void deleteUser(UUID userId);

    UserDto updateProfile(User user, ProfileUpdateDto dto);

    void requestPasswordReset(String email);
    void resetPassword(@NotEmpty(message = "Key cannot be empty or null") String key, @Valid PasswordDto passwordDto);

    User getAuthenticatedCustomer();
}