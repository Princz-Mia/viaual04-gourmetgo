package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Credential;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, UUID> {

    Credential findByUser_Id(UUID uuid);

    void deleteByUser_Id(UUID userId);

    Optional<Credential> findByUser(User user);
}
