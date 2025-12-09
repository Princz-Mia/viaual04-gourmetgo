package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    User findByEmailAddress(String emailAddress);

    boolean existsByEmailAddress(@NotBlank @Email String emailAddress);
    

    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= CURRENT_DATE")
    long countTodayRegistrations();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :weekAgo")
    long countActiveUsersThisWeek(@Param("weekAgo") LocalDateTime weekAgo);
    
    @Query("SELECT SUM(u.loginAttempts) FROM User u WHERE u.loginAttempts IS NOT NULL")
    Long sumLoginAttempts();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.loginAttempts > 3")
    Long countFailedLogins();
}
