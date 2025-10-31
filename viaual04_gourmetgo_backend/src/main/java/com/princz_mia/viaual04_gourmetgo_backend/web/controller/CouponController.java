package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICouponService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICustomerService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Coupon;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CouponDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/coupons")
@Slf4j
public class CouponController {

    private final ICouponService couponService;
    private final ICustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        LoggingUtils.logMethodEntry(log, "getAll");
        long startTime = System.currentTimeMillis();
        
        List<CouponDto> list = couponService.getAllCoupons();
        LoggingUtils.logBusinessEvent(log, "COUPONS_RETRIEVED", "count", list.size());
        LoggingUtils.logPerformance(log, "getAll", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Success", list));
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<ApiResponse> validateCoupon(@PathVariable String code) {
        LoggingUtils.logMethodEntry(log, "validateCoupon", "code", code);
        long startTime = System.currentTimeMillis();
        
        try {
            Customer customer = customerService.getAuthenticatedCustomer();
            Coupon coupon = couponService.validateCoupon(customer.getId(), code);
            CouponDto dto = couponService.convertCouponToDto(coupon);
            
            LoggingUtils.logBusinessEvent(log, "COUPON_VALIDATED", "couponCode", code, "customerId", customer.getId());
            LoggingUtils.logPerformance(log, "validateCoupon", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Coupon valid", dto));
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to validate coupon", e, "code", code);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody @Valid CouponDto couponDto) {
        LoggingUtils.logMethodEntry(log, "create", "code", couponDto.getCode());
        long startTime = System.currentTimeMillis();
        
        try {
            Coupon coupon = couponService.createCoupon(couponDto);
            CouponDto dto = couponService.convertCouponToDto(coupon);
            
            LoggingUtils.logBusinessEvent(log, "COUPON_CREATED", "couponId", coupon.getId(), "code", couponDto.getCode());
            LoggingUtils.logPerformance(log, "create", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Created", dto));
        } catch (AlreadyExistsException e) {
            LoggingUtils.logError(log, "Failed to create coupon - already exists", e, "code", couponDto.getCode());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable UUID id, @RequestBody CouponDto couponDto) {
        LoggingUtils.logMethodEntry(log, "update", "id", id, "code", couponDto.getCode());
        long startTime = System.currentTimeMillis();
        
        try {
            Coupon coupon = couponService.updateCoupon(id, couponDto);
            CouponDto dto = couponService.convertCouponToDto(coupon);
            
            LoggingUtils.logBusinessEvent(log, "COUPON_UPDATED", "couponId", id, "code", couponDto.getCode());
            LoggingUtils.logPerformance(log, "update", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Success", dto));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Failed to update coupon - not found", e, "id", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable UUID id) {
        LoggingUtils.logMethodEntry(log, "delete", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            couponService.deleteCoupon(id);
            
            LoggingUtils.logBusinessEvent(log, "COUPON_DELETED", "couponId", id);
            LoggingUtils.logPerformance(log, "delete", System.currentTimeMillis() - startTime);
            
            return ResponseEntity.ok(new ApiResponse("Success", null));
        } catch (ResourceNotFoundException e) {
            LoggingUtils.logError(log, "Failed to delete coupon - not found", e, "id", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
}