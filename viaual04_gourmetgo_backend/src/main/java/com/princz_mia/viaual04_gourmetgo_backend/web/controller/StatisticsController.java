package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IStatisticsService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.IPdfReportService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.config.security.CustomUserDetails;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.AdminDashboardDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantDashboardDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController {
    
    private final IStatisticsService statisticsService;
    private final IPdfReportService pdfReportService;
    
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAdminDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        LoggingUtils.logMethodEntry(log, "getAdminDashboard", "startDate", startDate, "endDate", endDate);
        long startTime = System.currentTimeMillis();
        
        try {
            // Default to last 30 days if not specified
            if (startDate == null) startDate = LocalDate.now().minusDays(30);
            if (endDate == null) endDate = LocalDate.now();
            
            AdminDashboardDto dashboard = statisticsService.getAdminDashboard(startDate, endDate);
            
            LoggingUtils.logBusinessEvent(log, "ADMIN_DASHBOARD_RETRIEVED", "success", true);
            LoggingUtils.logPerformance(log, "getAdminDashboard", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Success", dashboard));
        } catch (Exception e) {
            LoggingUtils.logError(log, "Error retrieving admin dashboard", e);
            return ResponseEntity.internalServerError().body(new ApiResponse("Error retrieving dashboard data", null));
        }
    }
    
    @GetMapping("/restaurant/{restaurantId}/dashboard")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<ApiResponse> getRestaurantDashboard(
            @PathVariable UUID restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        LoggingUtils.logMethodEntry(log, "getRestaurantDashboard", "restaurantId", restaurantId);
        long startTime = System.currentTimeMillis();
        
        try {
            // Verify restaurant access
            if (!userDetails.getUser().getId().equals(restaurantId)) {
                return ResponseEntity.status(403).body(new ApiResponse("Access denied", null));
            }
            
            // Default to last 30 days if not specified
            if (startDate == null) startDate = LocalDate.now().minusDays(30);
            if (endDate == null) endDate = LocalDate.now();
            
            RestaurantDashboardDto dashboard = statisticsService.getRestaurantDashboard(restaurantId, startDate, endDate);
            
            LoggingUtils.logBusinessEvent(log, "RESTAURANT_DASHBOARD_RETRIEVED", "restaurantId", restaurantId, "success", true);
            LoggingUtils.logPerformance(log, "getRestaurantDashboard", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Success", dashboard));
        } catch (Exception e) {
            LoggingUtils.logError(log, "Error retrieving restaurant dashboard", e, "restaurantId", restaurantId);
            return ResponseEntity.internalServerError().body(new ApiResponse("Error retrieving dashboard data", null));
        }
    }
    
    @GetMapping("/online-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getOnlineUsersCount() {
        try {
            Long activeUsers = statisticsService.getActiveUsersCount();
            return ResponseEntity.ok(new ApiResponse("Success", activeUsers));
        } catch (Exception e) {
            LoggingUtils.logError(log, "Error retrieving online users count", e);
            return ResponseEntity.internalServerError().body(new ApiResponse("Error retrieving online users", null));
        }
    }
    
    @GetMapping("/admin/report/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> generateAdminPdfReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String sections) {
        
        LoggingUtils.logMethodEntry(log, "generateAdminPdfReport", "startDate", startDate, "endDate", endDate);
        
        try {
            // Default to last 30 days if not specified
            if (startDate == null) startDate = LocalDate.now().minusDays(30);
            if (endDate == null) endDate = LocalDate.now();
            
            byte[] pdfBytes = pdfReportService.generateAdminReport(startDate, endDate, sections);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                String.format("admin-report-%s-to-%s.pdf", startDate, endDate));
            
            LoggingUtils.logBusinessEvent(log, "ADMIN_PDF_REPORT_GENERATED", "success", true);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            LoggingUtils.logError(log, "Error generating admin PDF report", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/restaurant/{restaurantId}/report/pdf")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<byte[]> generateRestaurantPdfReport(
            @PathVariable UUID restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String sections,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        LoggingUtils.logMethodEntry(log, "generateRestaurantPdfReport", "restaurantId", restaurantId);
        
        try {
            // Verify restaurant access
            if (!userDetails.getUser().getId().equals(restaurantId)) {
                return ResponseEntity.status(403).build();
            }
            
            // Default to last 30 days if not specified
            if (startDate == null) startDate = LocalDate.now().minusDays(30);
            if (endDate == null) endDate = LocalDate.now();
            
            byte[] pdfBytes = pdfReportService.generateRestaurantReport(restaurantId, startDate, endDate, sections);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                String.format("restaurant-report-%s-to-%s.pdf", startDate, endDate));
            
            LoggingUtils.logBusinessEvent(log, "RESTAURANT_PDF_REPORT_GENERATED", "restaurantId", restaurantId, "success", true);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            LoggingUtils.logError(log, "Error generating restaurant PDF report", e, "restaurantId", restaurantId);
            return ResponseEntity.internalServerError().build();
        }
    }
}