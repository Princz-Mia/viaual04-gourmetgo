package com.princz_mia.viaual04_gourmetgo_backend.web.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RewardPointDto {
    private UUID id;
    private UUID customerId;
    private BigDecimal balance;
    private LocalDateTime lastUpdated;
}