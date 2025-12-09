package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Order;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RewardPointDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RewardTransactionDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IRewardService {
    RewardPointDto getCustomerPoints(UUID customerId);
    List<RewardTransactionDto> getCustomerTransactions(UUID customerId);
    void awardPointsForOrder(Order order);
    BigDecimal redeemPoints(UUID customerId, BigDecimal pointsToRedeem);
    void creditPoints(UUID customerId, BigDecimal points, String description);
    void compensateFailedOrder(Order order);
    void processDeliveryReward(Order order);
    void processRefundReward(Order order);
    void addCompensationReward(UUID customerId, BigDecimal points, String description);
    void addPromotionReward(UUID customerId, BigDecimal points, String description);
    void addPromotionRewardToAll(BigDecimal points, String description);
    BigDecimal useRewardPoints(UUID customerId, BigDecimal pointsToUse, Order order);
    BigDecimal getRewardBalance(UUID customerId);
    java.util.List<com.princz_mia.viaual04_gourmetgo_backend.data.entity.RewardTransaction> getRewardHistory(UUID customerId);
}