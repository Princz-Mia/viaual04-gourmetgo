package com.princz_mia.viaual04_gourmetgo_backend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    User findByEmailAddress(String emailAddress);

    boolean existsByEmailAddress(@NotBlank @Email String emailAddress);
}
