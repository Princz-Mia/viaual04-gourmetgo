package com.princz_mia.viaual04_gourmetgo_backend.config.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @Email  
    private String emailAddress;

    @NotBlank
    private String password;
}
