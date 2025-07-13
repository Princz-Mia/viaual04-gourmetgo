package com.princz_mia.viaual04_gourmetgo_backend.customer;


import com.princz_mia.viaual04_gourmetgo_backend.cart.CartDto;
import com.princz_mia.viaual04_gourmetgo_backend.order.OrderDto;
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

    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private Integer loginAttempts;
    private boolean isAccountNonLocked;
    private boolean isEnabled;
}
