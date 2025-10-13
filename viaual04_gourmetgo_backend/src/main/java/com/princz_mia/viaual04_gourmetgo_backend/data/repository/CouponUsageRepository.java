package com.princz_mia.viaual04_gourmetgo_backend.coupon_usage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, UUID> {
    boolean existsByCoupon_CodeAndCustomer_Id(String code, UUID customerId);
}
