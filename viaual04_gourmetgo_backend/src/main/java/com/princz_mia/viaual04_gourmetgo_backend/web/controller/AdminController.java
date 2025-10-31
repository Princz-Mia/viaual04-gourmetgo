package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IAdminService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.AdminDto;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Admin;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/admins")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final IAdminService adminService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "getById", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Fetching admin with ID: {}", id);
            Admin admin = adminService.getAdminById(id);
            AdminDto adminDto = adminService.convertAdminToDto(admin);
            
            LoggingUtils.logBusinessEvent(log, "ADMIN_RETRIEVED", "adminId", id, "success", true);
            LoggingUtils.logPerformance(log, "getAdminById", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Success", adminDto));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Admin not found", e, "adminId", id);
            LoggingUtils.logBusinessEvent(log, "ADMIN_NOT_FOUND", "adminId", id, "error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
}