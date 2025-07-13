package com.princz_mia.viaual04_gourmetgo_backend.customer;

import lombok.Data;

@Data
public class CreateCustomerRequest {
    private String fullName;
    private String emailAddress;
    private String password;
}
