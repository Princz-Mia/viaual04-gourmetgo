package com.princz_mia.viaual04_gourmetgo_backend.payment_method;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDto {
    private UUID id;
    private String name;
}
