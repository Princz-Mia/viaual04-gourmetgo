package com.princz_mia.viaual04_gourmetgo_backend.coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    Coupon findByCode(String code);

    boolean existsByCode(String code);
}
