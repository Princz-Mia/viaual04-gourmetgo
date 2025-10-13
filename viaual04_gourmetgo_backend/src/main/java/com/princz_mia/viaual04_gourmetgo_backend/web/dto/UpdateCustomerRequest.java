package com.princz_mia.viaual04_gourmetgo_backend.web.dto;

import lombok.Data;

@Data
public class UpdateCustomerRequest {
    private String fullName;
    private String phoneNumber;
    private String emailAddress;
}