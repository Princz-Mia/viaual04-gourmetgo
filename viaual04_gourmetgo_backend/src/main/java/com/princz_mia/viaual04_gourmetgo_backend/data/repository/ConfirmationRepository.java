package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Confirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConfirmationRepository extends JpaRepository<Confirmation, UUID> {

    Confirmation findByKey(String key);
    Confirmation findByUser_Id(UUID userId);
}
