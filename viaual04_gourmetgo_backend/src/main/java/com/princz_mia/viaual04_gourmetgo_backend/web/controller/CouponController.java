package com.princz_mia.viaual04_gourmetgo_backend.coupon;

import com.princz_mia.viaual04_gourmetgo_backend.customer.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.customer.ICustomerService;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/coupons")
public class CouponController {

    private final ICouponService couponService;
    private final ICustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        List<CouponDto> list = couponService.getAllCoupons();
        return ResponseEntity.ok(new ApiResponse("Success", list));
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<ApiResponse> validateCoupon(
            @PathVariable String code
    ) {
        Customer customer = customerService.getAuthenticatedCustomer();

        Coupon coupon = couponService.validateCoupon(customer.getId(), code);
        CouponDto dto = couponService.convertCouponToDto(coupon);

        return ResponseEntity.ok(new ApiResponse("Coupon valid", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody @Valid CouponDto couponDto) {
        try {
            Coupon coupon = couponService.createCoupon(couponDto);
            CouponDto dto = couponService.convertCouponToDto(coupon);
            return ResponseEntity.ok(new ApiResponse("Created", dto));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(
            @PathVariable UUID id,
            @RequestBody CouponDto couponDto
    ) {
        try {
            Coupon coupon = couponService.updateCoupon(id, couponDto);
            CouponDto dto = couponService.convertCouponToDto(coupon);
            return ResponseEntity.ok(new ApiResponse("Success", dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable UUID id) {
        try {
            couponService.deleteCoupon(id);
            return ResponseEntity.ok(new ApiResponse("Success", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
}
