package com.princz_mia.viaual04_gourmetgo_backend.config.security;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordDto {
    @NotEmpty
    private String password;
    @NotEmpty
    private String confirmPassword;
}
