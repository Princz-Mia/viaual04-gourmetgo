package com.princz_mia.viaual04_gourmetgo_backend.config.security;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Restaurant;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RestaurantAccessControl {
    
    private final UserRepository userRepository;
    
    public boolean canAccess(Authentication authentication, UUID restaurantId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String userEmail = authentication.getName();
        var user = userRepository.findByEmailAddress(userEmail);
        
        if (user instanceof Restaurant restaurant) {
            return restaurant.getId().equals(restaurantId);
        }
        
        // Admins can access any restaurant
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}