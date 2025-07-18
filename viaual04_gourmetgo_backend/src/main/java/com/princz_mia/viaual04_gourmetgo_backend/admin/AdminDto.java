package com.princz_mia.viaual04_gourmetgo_backend.admin;

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
