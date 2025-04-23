package com.princz_mia.viaual04_gourmetgo_backend.restaurant;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class RestaurantDto {
    private UUID id;
    private String name;
    private String emailAddress;
    private String hours;
    private BigDecimal deliveryFee;
    private List<String> categories;
    private boolean isAccountNonLocked;
    private boolean isEnabled;
}
