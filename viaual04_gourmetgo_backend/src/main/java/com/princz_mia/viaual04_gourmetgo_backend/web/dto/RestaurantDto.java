package com.princz_mia.viaual04_gourmetgo_backend.web.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDto {
    private UUID id;
    private String name;
    private String phoneNumber;
    private String ownerName;
    private String emailAddress;
    @Min(0)
    private BigDecimal deliveryFee;
    private AddressDto address;
    private List<RestaurantCategoryDto> categories;
    private Map<DayOfWeek, RestaurantDto.HoursDto> openingHours;
    private List<ProductCategoryDto> productCategories;
    private ImageDto logo;
    private LocalDateTime createdAt;
    private boolean isApproved;
    private boolean isAccountNonLocked;
    private boolean isEnabled;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoursDto {
        private LocalTime openingTime;
        private LocalTime closingTime;
    }
}