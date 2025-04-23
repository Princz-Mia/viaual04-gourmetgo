package com.princz_mia.viaual04_gourmetgo_backend.admin;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdminDto {
    private UUID id;
    private String fullName;
    private String emailAddress;

    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private Integer loginAttempts;
    private boolean isAccountNonLocked;
    private boolean isEnabled;
}
