package com.princz_mia.viaual04_gourmetgo_backend.web.dto;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private UUID id;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String orderNotes;
    private String deliveryInstructions;
    private CouponDto coupon;
    private Set<OrderItemDto> orderItems;

    private BillingInfoDto billingInformation;
    private ShippingInfoDto shippingInformation;

    private PaymentMethodDto paymentMethod;

    private UUID customerId;
    private RestaurantDto restaurant;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingInfoDto {
        private String fullName;
        private String phoneNumber;
        private AddressDto address;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingInfoDto {
        private String fullName;
        private String phoneNumber;
        private AddressDto address;
    }
}