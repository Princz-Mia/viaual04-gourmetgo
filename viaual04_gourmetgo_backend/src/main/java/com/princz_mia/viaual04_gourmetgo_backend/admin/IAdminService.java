package com.princz_mia.viaual04_gourmetgo_backend.admin;

import java.util.UUID;

public interface IAdminService {
    Admin getAdminById(UUID id);

    AdminDto convertAdminToDto(Admin admin);
}
