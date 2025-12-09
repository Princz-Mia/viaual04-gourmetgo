package com.princz_mia.viaual04_gourmetgo_backend.config.logging;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IStatisticsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RequestTrackingFilter extends OncePerRequestFilter {
    
    private final IStatisticsService statisticsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Track request for statistics
            try {
                statisticsService.trackRequest(
                    request.getRequestURI(),
                    request.getMethod(),
                    response.getStatus(),
                    responseTime,
                    request.getHeader("User-Agent"),
                    getClientIpAddress(request)
                );
            } catch (Exception e) {
                // Don't let statistics tracking break the request
                logger.warn("Failed to track request statistics", e);
            }
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}