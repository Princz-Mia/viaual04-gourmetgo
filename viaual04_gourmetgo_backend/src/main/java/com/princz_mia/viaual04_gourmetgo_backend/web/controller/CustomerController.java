package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICustomerService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CreateCustomerRequest;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CustomerDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.UpdateCustomerRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/customers")
public class CustomerController {

    private final ICustomerService customerService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCustomerById(@PathVariable UUID id) {
        Customer customer = customerService.getCustomerById(id);
        CustomerDto customerDto = customerService.convertCustomerToDto(customer);
        return ResponseEntity.ok(new ApiResponse("Customer retrieved successfully", customerDto));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = customerService.createCustomer(request);
        CustomerDto customerDto = customerService.convertCustomerToDto(customer);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Customer registered successfully", customerDto));
    }

    @GetMapping("/verify/account")
    public ResponseEntity<ApiResponse> verifyUserAccount(
            @RequestParam("key") @NotEmpty(message = "Key cannot be empty or null") String key) {
        customerService.verifyAccountKey(key);
        return ResponseEntity.ok(new ApiResponse("Account verified successfully", null));
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<ApiResponse> updateCustomer(
            @Valid @RequestBody UpdateCustomerRequest request, 
            @PathVariable UUID customerId) {
        Customer customer = customerService.updateCustomer(request, customerId);
        CustomerDto customerDto = customerService.convertCustomerToDto(customer);
        return ResponseEntity.ok(new ApiResponse("Customer updated successfully", customerDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(new ApiResponse("Customer deleted successfully", null));
    }
}
