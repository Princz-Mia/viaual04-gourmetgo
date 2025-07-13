package com.princz_mia.viaual04_gourmetgo_backend.customer;

import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.response.ApiResponse;
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
        try {
            Customer customer = customerService.getCustomerById(id);
            CustomerDto customerDto = customerService.convertCustomerToDto(customer);
            return ResponseEntity.ok(new ApiResponse("Success", customerDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerCustomer(@RequestBody CreateCustomerRequest request) {
        try {
            Customer customer = customerService.createCustomer(request);
            CustomerDto customerDto = customerService.convertCustomerToDto(customer);
            return ResponseEntity.ok(new ApiResponse("Success", customerDto));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/verify/account")
    public ResponseEntity<ApiResponse> verifyUserAccount(@RequestParam("key") @NotEmpty(message = "Key cannot be empty or null") String key) {
        try {
            customerService.verifyAccountKey(key);
            return ResponseEntity.ok().body(new ApiResponse("Account verified successfully", HttpStatus.OK));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<ApiResponse> updateCustomer(@RequestBody UpdateCustomerRequest request, @PathVariable UUID customerId) {
        try {
            Customer customer = customerService.updateCustomer(request, customerId);
            CustomerDto customerDto = customerService.convertCustomerToDto(customer);
            return ResponseEntity.ok(new ApiResponse("Success", customerDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCustomer(@PathVariable UUID id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok(new ApiResponse("Success", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
}
