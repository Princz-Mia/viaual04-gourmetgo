package com.princz_mia.viaual04_gourmetgo_backend.review;

import java.util.List;
import java.util.UUID;

public interface IReviewService {
    List<ReviewDto> getReviewsForRestaurant(UUID restaurantId);

    ReviewDto addReview(UUID customerId, UUID restaurantId, Integer rating, String comment);

    ReviewDto convertReviewToDto(Review review);
}
