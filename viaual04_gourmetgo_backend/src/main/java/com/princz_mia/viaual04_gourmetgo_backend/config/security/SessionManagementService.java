package com.princz_mia.viaual04_gourmetgo_backend.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SessionManagementService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_SESSIONS_PER_USER = 3;
    private static final String SESSION_PREFIX = "user_sessions:";
    
    public boolean canCreateNewSession(String userEmail) {
        try {
            String key = SESSION_PREFIX + userEmail;
            Long sessionCount = redisTemplate.opsForSet().size(key);
            return sessionCount == null || sessionCount < MAX_SESSIONS_PER_USER;
        } catch (Exception e) {
            // If Redis is not available, allow session creation
            return true;
        }
    }
    
    public void addSession(String userEmail, String sessionId) {
        try {
            String key = SESSION_PREFIX + userEmail;
            redisTemplate.opsForSet().add(key, sessionId);
            redisTemplate.expire(key, 7, TimeUnit.DAYS); // Match refresh token expiry
        } catch (Exception e) {
            // Ignore Redis errors for session management
        }
    }
    
    public void removeSession(String userEmail, String sessionId) {
        try {
            String key = SESSION_PREFIX + userEmail;
            redisTemplate.opsForSet().remove(key, sessionId);
        } catch (Exception e) {
            // Ignore Redis errors for session management
        }
    }
    
    public void removeAllSessions(String userEmail) {
        try {
            String key = SESSION_PREFIX + userEmail;
            redisTemplate.delete(key);
        } catch (Exception e) {
            // Ignore Redis errors for session management
        }
    }
}