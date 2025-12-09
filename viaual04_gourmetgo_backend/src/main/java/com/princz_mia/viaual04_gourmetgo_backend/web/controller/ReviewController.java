package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICustomerService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.IReviewService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ReviewDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final IReviewService reviewService;
    private final ICustomerService customerService;

    @GetMapping("/by-restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse> getByRestaurant(@PathVariable UUID restaurantId) {
        LoggingUtils.logMethodEntry(log, "getByRestaurant", "restaurantId", restaurantId);
        List<ReviewDto> reviews = reviewService.getReviewsForRestaurant(restaurantId);
        LoggingUtils.logBusinessEvent(log, "REVIEWS_RETRIEVED", "restaurantId", restaurantId, "count", reviews.size());
        return ResponseEntity.ok(new ApiResponse("Success", reviews));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addReview(
            @RequestParam UUID restaurantId,
            @RequestParam @Min(1) @Max(5) Integer rating,
            @RequestParam(required = false) String comment
    ) {
        LoggingUtils.logMethodEntry(log, "addReview", "restaurantId", restaurantId, "rating", rating);
        Customer customer = customerService.getAuthenticatedCustomer();
        ReviewDto dto = reviewService.addReview(customer.getId(), restaurantId, rating, comment);
        LoggingUtils.logBusinessEvent(log, "REVIEW_ADDED", "reviewId", dto.getId(), "customerId", customer.getId(), "restaurantId", restaurantId, "rating", rating);
        return ResponseEntity.ok(new ApiResponse("Review added", dto));
    }

    @DeleteMapping("/delete/{restaurantId}")
    public ResponseEntity<ApiResponse> deleteReview(@PathVariable UUID restaurantId) {
        Customer customer = customerService.getAuthenticatedCustomer();
        reviewService.deleteReview(customer.getId(), restaurantId);
        return ResponseEntity.ok(new ApiResponse("Review deleted", null));
    }

    @GetMapping("/my-review/{restaurantId}")
    public ResponseEntity<ApiResponse> getMyReview(@PathVariable UUID restaurantId) {
        Customer customer = customerService.getAuthenticatedCustomer();
        ReviewDto review = reviewService.getCustomerReview(customer.getId(), restaurantId);
        return ResponseEntity.ok(new ApiResponse("Success", review));
    }
}
