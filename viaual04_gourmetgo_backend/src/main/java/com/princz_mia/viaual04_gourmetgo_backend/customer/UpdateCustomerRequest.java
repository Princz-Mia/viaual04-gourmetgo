package com.princz_mia.viaual04_gourmetgo_backend.customer;

import lombok.Data;

@Data
public class UpdateCustomerRequest {
    private String fullName;
    private String phoneNumber;
    private String emailAddress;
}
