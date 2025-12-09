package com.princz_mia.viaual04_gourmetgo_backend.web.dto;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private UUID id;
    private LocalDateTime orderDate;
    
    @DecimalMin(value = "0.0", message = "Total amount cannot be negative")
    private BigDecimal totalAmount;
    
    private OrderStatus status;
    
    @Size(max = 500, message = "Order notes must not exceed 500 characters")
    private String orderNotes;
    
    @Size(max = 500, message = "Delivery instructions must not exceed 500 characters")
    private String deliveryInstructions;
    
    @Valid
    private CouponDto coupon;
    
    @Valid
    @NotEmpty(message = "Order must contain at least one item")
    private Set<OrderItemDto> orderItems;

    @Valid
    @NotNull(message = "Billing information is required")
    private BillingInfoDto billingInformation;

    @Valid
    private ShippingInfoDto shippingInformation;

    @Valid
    @NotNull(message = "Payment method is required")
    private PaymentMethodDto paymentMethod;

    private UUID customerId;
    
    @Valid
    @NotNull(message = "Restaurant is required")
    private RestaurantDto restaurant;
    
    @DecimalMin(value = "0.0", message = "Points to redeem cannot be negative")
    private BigDecimal pointsToRedeem;
    
    @DecimalMin(value = "0.0", message = "Used reward points cannot be negative")
    private BigDecimal usedRewardPoints;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingInfoDto {
        @NotBlank(message = "Full name is required for billing")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        private String fullName;
        
        @NotBlank(message = "Phone number is required for billing")
        @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Invalid phone number format")
        private String phoneNumber;
        
        @Valid
        @NotNull(message = "Billing address is required")
        private AddressDto address;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingInfoDto {
        @NotBlank(message = "Full name is required for shipping")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        private String fullName;
        
        @NotBlank(message = "Phone number is required for shipping")
        @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Invalid phone number format")
        private String phoneNumber;
        
        @Valid
        @NotNull(message = "Shipping address is required")
        private AddressDto address;
    }
}