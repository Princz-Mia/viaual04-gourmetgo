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
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService
{

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsForRestaurant(UUID restaurantId) {
        return reviewRepository.findAllByRestaurant_Id(restaurantId)
                .stream()
                .map(this::convertReviewToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewDto addReview(UUID customerId, UUID restaurantId, Integer rating, String comment) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found", HttpStatus.NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found", HttpStatus.NOT_FOUND));

        boolean hasOrdered = orderRepository.existsByCustomer_IdAndRestaurant_Id(customerId, restaurantId);
        if (!hasOrdered) {
            throw new ResourceNotFoundException("Order not found from restaurant", HttpStatus.NOT_FOUND);
        }

        Review rev = Review.builder()
                .customer(customer)
                .restaurant(restaurant)
                .ratingValue(rating)
                .comment(comment)
                .build();

        Review savedReview = reviewRepository.save(rev);
        return convertReviewToDto(savedReview);
    }

    @Override
    public ReviewDto convertReviewToDto(Review review) {
        return modelMapper.map(review, ReviewDto.class);
    }
}