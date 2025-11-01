package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Admin;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.AdminDto;

import java.util.UUID;

public interface IAdminService {
    Admin getAdminById(UUID id);

    AdminDto convertAdminToDto(Admin admin);
}
