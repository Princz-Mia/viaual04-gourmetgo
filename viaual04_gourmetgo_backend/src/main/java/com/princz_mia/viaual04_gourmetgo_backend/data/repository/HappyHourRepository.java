package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.HappyHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface HappyHourRepository extends JpaRepository<HappyHour, Long> {
    
    @Query("SELECT hh FROM HappyHour hh WHERE hh.active = true AND ?1 BETWEEN hh.startTime AND hh.endTime")
    Optional<HappyHour> findActiveHappyHour(LocalDateTime now);
}