package com.princz_mia.viaual04_gourmetgo_backend.web.dto;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.RewardTransaction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RewardTransactionDto {
    private UUID id;
    private UUID customerId;
    private RewardTransaction.TransactionType type;
    private BigDecimal amount;
    private String description;
    private UUID orderId;
    private LocalDateTime createdAt;
}