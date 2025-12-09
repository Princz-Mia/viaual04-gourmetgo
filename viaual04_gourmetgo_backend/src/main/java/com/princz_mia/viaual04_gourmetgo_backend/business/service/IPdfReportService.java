package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.web.dto.AdminDashboardDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantDashboardDto;

import java.time.LocalDate;
import java.util.UUID;

public interface IPdfReportService {
    
    byte[] generateAdminReport(LocalDate startDate, LocalDate endDate, String sections);
    
    byte[] generateRestaurantReport(UUID restaurantId, LocalDate startDate, LocalDate endDate, String sections);
}