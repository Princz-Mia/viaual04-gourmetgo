package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Coupon;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CouponDto;
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