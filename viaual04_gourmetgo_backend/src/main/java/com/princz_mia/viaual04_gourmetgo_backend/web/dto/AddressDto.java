package com.princz_mia.viaual04_gourmetgo_backend.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private UUID id;
    private String region;
    private String postalCode;
    private String city;
    private String addressLine;
    private String unitNumber;
}