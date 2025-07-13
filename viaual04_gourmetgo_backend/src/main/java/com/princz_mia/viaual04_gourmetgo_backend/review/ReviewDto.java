package com.princz_mia.viaual04_gourmetgo_backend.review;

import com.princz_mia.viaual04_gourmetgo_backend.customer.CustomerDto;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant.RestaurantDto;
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
public class ReviewDto {
    private UUID id;
    private CustomerDto customer;
    private RestaurantDto restaurant;
    private Integer ratingValue;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
