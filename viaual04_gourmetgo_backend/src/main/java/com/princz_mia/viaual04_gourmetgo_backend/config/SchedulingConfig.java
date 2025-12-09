package com.princz_mia.viaual04_gourmetgo_backend.config;

import com.princz_mia.viaual04_gourmetgo_backend.data.repository.ActiveSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulingConfig {
    
    private final ActiveSessionRepository activeSessionRepository;
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void cleanupInactiveSessions() {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusHours(1);
            activeSessionRepository.deleteInactiveSessions(threshold);
            log.debug("Cleaned up inactive sessions older than {}", threshold);
        } catch (Exception e) {
            log.error("Error cleaning up inactive sessions", e);
        }
    }
}