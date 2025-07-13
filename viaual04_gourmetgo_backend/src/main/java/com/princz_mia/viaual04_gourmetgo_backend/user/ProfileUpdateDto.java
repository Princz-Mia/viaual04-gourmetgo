package com.princz_mia.viaual04_gourmetgo_backend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateDto {
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String emailAddress;
}
