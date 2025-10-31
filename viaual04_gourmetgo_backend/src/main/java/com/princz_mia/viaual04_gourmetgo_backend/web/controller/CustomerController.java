package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICustomerService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CreateCustomerRequest;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CustomerDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.UpdateCustomerRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/customers")
@Slf4j
public class CustomerController {

    private final ICustomerService customerService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCustomerById(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "getCustomerById", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Retrieving customer with ID: {}", id);
            Customer customer = customerService.getCustomerById(id);
            CustomerDto customerDto = customerService.convertCustomerToDto(customer);
            
            LoggingUtils.logBusinessEvent(log, "CUSTOMER_RETRIEVED", "customerId", id, "success", true);
            LoggingUtils.logPerformance(log, "getCustomerById", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Customer retrieved successfully", customerDto));
        } catch (Exception e) {
            LoggingUtils.logError(log, "Error retrieving customer", e, "customerId", id);
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        LoggingUtils.logMethodEntry(log, "registerCustomer", "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Registering new customer with email: {}", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
            Customer customer = customerService.createCustomer(request);
            CustomerDto customerDto = customerService.convertCustomerToDto(customer);
            
            LoggingUtils.logBusinessEvent(log, "CUSTOMER_REGISTERED", "customerId", customer.getId(), "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
            LoggingUtils.logPerformance(log, "registerCustomer", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("Customer registered successfully", customerDto));
        } catch (Exception e) {
            LoggingUtils.logError(log, "Error registering customer", e, "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
            throw e;
        }
    }

    @GetMapping("/verify/account")
    public ResponseEntity<ApiResponse> verifyUserAccount(
            @RequestParam("key") @NotEmpty(message = "Key cannot be empty or null") String key) {
        LoggingUtils.logMethodEntry(log, "verifyUserAccount", "key", key);
        long startTime = System.currentTimeMillis();
        
        try {
            customerService.verifyAccountKey(key);
            
            LoggingUtils.logBusinessEvent(log, "CUSTOMER_ACCOUNT_VERIFIED", "verificationKey", key);
            LoggingUtils.logPerformance(log, "verifyUserAccount", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Account verified successfully", null));
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to verify account", e, "key", key);
            throw e;
        }
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<ApiResponse> updateCustomer(
            @Valid @RequestBody UpdateCustomerRequest request, 
            @PathVariable UUID customerId) {
        LoggingUtils.logMethodEntry(log, "updateCustomer", "customerId", customerId, "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
        long startTime = System.currentTimeMillis();
        
        try {
            Customer customer = customerService.updateCustomer(request, customerId);
            CustomerDto customerDto = customerService.convertCustomerToDto(customer);
            
            LoggingUtils.logBusinessEvent(log, "CUSTOMER_UPDATED", "customerId", customerId, "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
            LoggingUtils.logPerformance(log, "updateCustomer", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Customer updated successfully", customerDto));
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to update customer", e, "customerId", customerId);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCustomer(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "deleteCustomer", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            customerService.deleteCustomer(id);
            
            LoggingUtils.logBusinessEvent(log, "CUSTOMER_DELETED", "customerId", id);
            LoggingUtils.logPerformance(log, "deleteCustomer", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Customer deleted successfully", null));
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to delete customer", e, "customerId", id);
            throw e;
        }
    }
}