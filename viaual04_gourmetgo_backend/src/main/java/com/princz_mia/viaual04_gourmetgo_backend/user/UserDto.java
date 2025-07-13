package com.princz_mia.viaual04_gourmetgo_backend.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String fullName;
    private String emailAddress;
    private String role;

    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private Integer loginAttempts;
    private boolean isAccountNonLocked;
    private boolean isEnabled;
}