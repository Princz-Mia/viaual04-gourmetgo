package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.web.dto.AdminDashboardDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantDashboardDto;

import java.time.LocalDate;
import java.util.UUID;

public interface IStatisticsService {
    
    AdminDashboardDto getAdminDashboard(LocalDate startDate, LocalDate endDate);
    
    RestaurantDashboardDto getRestaurantDashboard(UUID restaurantId, LocalDate startDate, LocalDate endDate);
    
    void trackVisit(String sessionId, String userEmail, String ipAddress, String userAgent);
    
    void trackRequest(String endpoint, String method, Integer statusCode, Long responseTime, String userAgent, String ipAddress);
    
    void updateSessionActivity(String sessionId);
    
    void removeSession(String sessionId);
    
    Long getActiveUsersCount();
}