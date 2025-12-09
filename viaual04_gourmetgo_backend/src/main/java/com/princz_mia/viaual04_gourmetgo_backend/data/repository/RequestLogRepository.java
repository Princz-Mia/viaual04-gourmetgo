package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, UUID> {
    
    @Query("SELECT COUNT(r) FROM RequestLog r WHERE r.timestamp BETWEEN :startTime AND :endTime")
    Long countRequestsBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT AVG(r.responseTime) FROM RequestLog r WHERE r.timestamp BETWEEN :startTime AND :endTime")
    Double averageResponseTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(r) * 100.0 / (SELECT COUNT(r2) FROM RequestLog r2 WHERE r2.timestamp BETWEEN :startTime AND :endTime) " +
           "FROM RequestLog r WHERE r.statusCode >= 400 AND r.timestamp BETWEEN :startTime AND :endTime")
    Double errorRateBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT r.endpoint, COUNT(r) as requestCount FROM RequestLog r " +
           "WHERE r.timestamp BETWEEN :startTime AND :endTime " +
           "GROUP BY r.endpoint ORDER BY requestCount DESC")
    List<Object[]> topEndpointsBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}