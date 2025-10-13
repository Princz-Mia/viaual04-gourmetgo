package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService implements IAdminService {

    private final AdminRepository adminRepository;
    private final ModelMapper modelMapper;

    @Override
    public Admin getAdminById(UUID id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public AdminDto convertAdminToDto(Admin admin) {
        return modelMapper.map(admin, AdminDto.class);
    }
}
