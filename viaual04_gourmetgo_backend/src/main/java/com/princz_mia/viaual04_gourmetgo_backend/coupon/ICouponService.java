package com.princz_mia.viaual04_gourmetgo_backend.coupon;

import com.princz_mia.viaual04_gourmetgo_backend.customer.Customer;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface ICouponService {
    List<CouponDto> getAllCoupons();

    Coupon createCoupon(CouponDto couponDto);

    Coupon updateCoupon(UUID id, CouponDto couponDto);

    void deleteCoupon(UUID id);

    @Transactional(readOnly = true)
    Coupon validateCoupon(UUID customerId, String code);

    @Transactional
    void recordUsage(Coupon coupon, Customer customer);

    CouponDto convertCouponToDto(Coupon coupon);
}
