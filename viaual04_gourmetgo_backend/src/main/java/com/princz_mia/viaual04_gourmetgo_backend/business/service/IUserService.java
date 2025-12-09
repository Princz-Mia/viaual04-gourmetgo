package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.web.dto.PasswordDto;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.User;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ProfileUpdateDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    List<UserDto> getAllUsers();
    UserDto getUserById(UUID userId);
    void lockUser(UUID userId, boolean locked);
    void deleteUser(UUID userId);

    UserDto updateProfile(User user, ProfileUpdateDto dto);

    void requestPasswordReset(String email);
    void resetPassword(@NotEmpty(message = "Key cannot be empty or null") String key, @Valid PasswordDto passwordDto);

    User getAuthenticatedCustomer();
}