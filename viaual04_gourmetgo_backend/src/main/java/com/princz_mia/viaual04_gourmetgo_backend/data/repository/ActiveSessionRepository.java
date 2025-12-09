package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.ActiveSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActiveSessionRepository extends JpaRepository<ActiveSession, UUID> {
    
    Optional<ActiveSession> findBySessionId(String sessionId);
    
    @Query("SELECT COUNT(s) FROM ActiveSession s WHERE s.lastActivity > :threshold")
    Long countActiveSessions(@Param("threshold") LocalDateTime threshold);
    
    @Modifying
    @Query("DELETE FROM ActiveSession s WHERE s.lastActivity < :threshold")
    void deleteInactiveSessions(@Param("threshold") LocalDateTime threshold);
    
    void deleteBySessionId(String sessionId);
}