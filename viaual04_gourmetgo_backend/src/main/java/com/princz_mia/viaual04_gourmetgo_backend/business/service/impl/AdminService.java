package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IAdminService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Admin;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.AdminRepository;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.AdminDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService implements IAdminService {

    private final AdminRepository adminRepository;
    private final ModelMapper modelMapper;

    @Override
    public Admin getAdminById(UUID id) {
        LoggingUtils.logMethodEntry(log, "getAdminById", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            Admin admin = adminRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
            
            LoggingUtils.logBusinessEvent(log, "ADMIN_RETRIEVED", "adminId", id);
            LoggingUtils.logPerformance(log, "getAdminById", System.currentTimeMillis() - startTime);
            
            return admin;
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Admin not found", e, "adminId", id);
            throw e;
        }
    }

    @Override
    public AdminDto convertAdminToDto(Admin admin) {
        return modelMapper.map(admin, AdminDto.class);
    }
}