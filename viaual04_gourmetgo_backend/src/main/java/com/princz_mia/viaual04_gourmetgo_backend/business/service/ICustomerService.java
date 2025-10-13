package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CreateCustomerRequest;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CustomerDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.UpdateCustomerRequest;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public interface ICustomerService {

    Customer getCustomerById(UUID id);

    Customer createCustomer(CreateCustomerRequest request);

    Customer updateCustomer(UpdateCustomerRequest request, UUID id);

    void deleteCustomer(UUID id);

    CustomerDto convertCustomerToDto(Customer customer);

    Customer getAuthenticatedCustomer();

    void verifyAccountKey(@NotEmpty(message = "Key cannot be empty or null") String key);

    List<Customer> getAllEnabledAndNonLockedCustomer();
}