package com.princz_mia.viaual04_gourmetgo_backend.web.dto;

import lombok.Data;

@Data
public class CreateCustomerRequest {
    private String fullName;
    private String emailAddress;
    private String password;
}