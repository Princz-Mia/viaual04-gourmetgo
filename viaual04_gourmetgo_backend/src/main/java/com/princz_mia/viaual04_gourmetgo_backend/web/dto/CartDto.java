package com.princz_mia.viaual04_gourmetgo_backend.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private UUID id;
    private Set<CartItemDto> items;
    private BigDecimal totalAmount;
}