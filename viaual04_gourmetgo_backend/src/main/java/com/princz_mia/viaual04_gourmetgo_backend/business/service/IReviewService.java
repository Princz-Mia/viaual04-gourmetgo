package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Review;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ReviewDto;

import java.util.List;
import java.util.UUID;

public interface IReviewService {
    List<ReviewDto> getReviewsForRestaurant(UUID restaurantId);

    ReviewDto addReview(UUID customerId, UUID restaurantId, Integer rating, String comment);

    void deleteReview(UUID customerId, UUID restaurantId);

    ReviewDto getCustomerReview(UUID customerId, UUID restaurantId);

    ReviewDto convertReviewToDto(Review review);
}