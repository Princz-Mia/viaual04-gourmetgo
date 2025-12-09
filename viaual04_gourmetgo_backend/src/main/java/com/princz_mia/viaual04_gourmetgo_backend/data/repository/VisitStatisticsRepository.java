package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.VisitStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VisitStatisticsRepository extends JpaRepository<VisitStatistics, UUID> {
    
    Optional<VisitStatistics> findByDate(LocalDate date);
    
    List<VisitStatistics> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(v.totalVisits) FROM VisitStatistics v WHERE v.date BETWEEN :startDate AND :endDate")
    Long sumTotalVisitsBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}