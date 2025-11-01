package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    boolean existsByEmailAddress(@Email String emailAddress);

    Customer findByEmailAddress(@Email String emailAddress);

    Customer findByEmailAddressIgnoreCase(@Email String emailAddress);

    List<Customer> findByIsEnabledAndIsAccountNonLocked(boolean isEnabled, boolean isAccountNonLocked);
}
