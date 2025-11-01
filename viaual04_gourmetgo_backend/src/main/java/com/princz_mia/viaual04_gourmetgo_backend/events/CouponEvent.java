package com.princz_mia.viaual04_gourmetgo_backend.events;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CouponEvent {

    private Coupon coupon;
    private EventType type;
}
