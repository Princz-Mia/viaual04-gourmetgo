package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {
    Admin findByEmailAddress(String emailAddress);
}
