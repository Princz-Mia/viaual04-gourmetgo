package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IReviewService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Restaurant;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Review;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.CustomerRepository;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.OrderRepository;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.RestaurantRepository;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.ReviewRepository;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ReviewDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsForRestaurant(UUID restaurantId) {
        LoggingUtils.logMethodEntry(log, "getReviewsForRestaurant", "restaurantId", restaurantId);
        List<ReviewDto> reviews = reviewRepository.findAllByRestaurant_Id(restaurantId)
                .stream()
                .map(this::convertReviewToDto)
                .collect(Collectors.toList());
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_REVIEWS_RETRIEVED", "restaurantId", restaurantId, "count", reviews.size());
        return reviews;
    }

    @Override
    @Transactional
    public ReviewDto addReview(UUID customerId, UUID restaurantId, Integer rating, String comment) {
        LoggingUtils.logMethodEntry(log, "addReview", "customerId", customerId, "restaurantId", restaurantId, "rating", rating);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        boolean hasOrdered = orderRepository.existsByCustomer_IdAndRestaurant_Id(customerId, restaurantId);
        if (!hasOrdered) {
            throw new ResourceNotFoundException("Order not found from restaurant");
        }

        Review rev = Review.builder()
                .customer(customer)
                .restaurant(restaurant)
                .ratingValue(rating)
                .comment(comment)
                .build();

        Review savedReview = reviewRepository.save(rev);
        LoggingUtils.logBusinessEvent(log, "REVIEW_ADDED", "reviewId", savedReview.getId(), "customerId", customerId, "restaurantId", restaurantId, "rating", rating);
        return convertReviewToDto(savedReview);
    }

    @Override
    public ReviewDto convertReviewToDto(Review review) {
        LoggingUtils.logMethodEntry(log, "convertReviewToDto", "reviewId", review.getId());
        return modelMapper.map(review, ReviewDto.class);
    }
}