package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ISystemHealthService;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/system")
@RequiredArgsConstructor
@Slf4j
public class SystemHealthController {
    
    private final ISystemHealthService systemHealthService;
    
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getSystemHealth() {
        try {
            Map<String, Object> metrics = systemHealthService.getSystemMetrics();
            return ResponseEntity.ok(new ApiResponse("Success", metrics));
        } catch (Exception e) {
            log.error("Error retrieving system health", e);
            return ResponseEntity.internalServerError().body(new ApiResponse("Error retrieving system health", null));
        }
    }
    
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getRecentLogs(@RequestParam(defaultValue = "50") int limit) {
        try {
            List<Map<String, Object>> logs = systemHealthService.getRecentLogs(limit);
            return ResponseEntity.ok(new ApiResponse("Success", logs));
        } catch (Exception e) {
            log.error("Error retrieving logs", e);
            return ResponseEntity.internalServerError().body(new ApiResponse("Error retrieving logs", null));
        }
    }
    
    @GetMapping("/logs/files")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getLogFiles() {
        try {
            List<String> logFiles = systemHealthService.getLogFiles();
            return ResponseEntity.ok(new ApiResponse("Success", logFiles));
        } catch (Exception e) {
            log.error("Error retrieving log files", e);
            return ResponseEntity.internalServerError().body(new ApiResponse("Error retrieving log files", null));
        }
    }
    
    @GetMapping("/logs/file/{filename}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getLogFileContent(@PathVariable String filename) {
        try {
            String content = systemHealthService.getLogFileContent(filename);
            return ResponseEntity.ok(new ApiResponse("Success", content));
        } catch (Exception e) {
            log.error("Error retrieving log file content", e);
            return ResponseEntity.internalServerError().body(new ApiResponse("Error retrieving log file content", null));
        }
    }
    
    @GetMapping("/user-analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getUserAnalytics() {
        try {
            Map<String, Object> analytics = systemHealthService.getUserAnalytics();
            return ResponseEntity.ok(new ApiResponse("Success", analytics));
        } catch (Exception e) {
            log.error("Error retrieving user analytics", e);
            return ResponseEntity.internalServerError().body(new ApiResponse("Error retrieving user analytics", null));
        }
    }
    
    @GetMapping("/business-insights")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getBusinessInsights() {
        try {
            Map<String, Object> insights = systemHealthService.getBusinessInsights();
            return ResponseEntity.ok(new ApiResponse("Success", insights));
        } catch (Exception e) {
            log.error("Error retrieving business insights", e);
            return ResponseEntity.internalServerError().body(new ApiResponse("Error retrieving business insights", null));
        }
    }
}