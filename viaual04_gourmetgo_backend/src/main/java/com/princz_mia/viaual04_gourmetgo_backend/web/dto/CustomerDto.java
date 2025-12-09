package com.princz_mia.viaual04_gourmetgo_backend.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private UUID id;
    private String fullName;
    private String emailAddress;
    private String phoneNumber;
    private List<OrderDto> orders;
    private CartDto cart;
    private RewardPointDto rewardPoint;

    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private Integer loginAttempts;
    private boolean isAccountNonLocked;
    private boolean isEnabled;
}