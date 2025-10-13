package com.princz_mia.viaual04_gourmetgo_backend.web.dto;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.CouponType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponDto {
    private UUID id;
    @NotEmpty
    private String code;
    private CouponType type;
    private BigDecimal value;
    @FutureOrPresent(message = "Expiration date must be today or in the future")
    private LocalDate expirationDate;
}