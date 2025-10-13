package com.princz_mia.viaual04_gourmetgo_backend.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    List<Restaurant> findByIsEnabled(boolean isEnabled);

    List<Restaurant> findByIsApproved(boolean isApproved);

    Restaurant findByEmailAddressIgnoreCase(String emailAddress);
}
