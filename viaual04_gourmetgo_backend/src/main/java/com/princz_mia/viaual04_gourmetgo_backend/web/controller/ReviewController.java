package com.princz_mia.viaual04_gourmetgo_backend.review;

import com.princz_mia.viaual04_gourmetgo_backend.customer.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.customer.ICustomerService;
import com.princz_mia.viaual04_gourmetgo_backend.response.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;
    private final ICustomerService customerService;

    @GetMapping("/by-restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse> getByRestaurant(@PathVariable UUID restaurantId) {
        List<ReviewDto> reviews = reviewService.getReviewsForRestaurant(restaurantId);
        return ResponseEntity.ok(new ApiResponse("Success", reviews));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addReview(
            @RequestParam UUID restaurantId,
            @RequestParam @Min(1) @Max(5) Integer rating,
            @RequestParam(required = false) String comment
    ) {
        Customer customer = customerService.getAuthenticatedCustomer();
        ReviewDto dto = reviewService.addReview(customer.getId(), restaurantId, rating, comment);
        return ResponseEntity.ok(new ApiResponse("Review added", dto));
    }
}
