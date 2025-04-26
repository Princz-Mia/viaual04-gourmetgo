package com.princz_mia.viaual04_gourmetgo_backend.config.security;

import lombok.Data;
import java.util.UUID;

@Data
public class LoginResponse {

    private UUID id;
    private String token;
}
