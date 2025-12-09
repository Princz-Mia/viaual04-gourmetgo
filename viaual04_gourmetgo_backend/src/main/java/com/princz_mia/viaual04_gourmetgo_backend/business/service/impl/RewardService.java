package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IRewardService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.*;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.*;
import com.princz_mia.viaual04_gourmetgo_backend.exception.BusinessRuleException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RewardPointDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RewardTransactionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RewardService implements IRewardService {

    private final RewardPointRepository rewardPointRepository;
    private final RewardTransactionRepository rewardTransactionRepository;
    private final CustomerRepository customerRepository;
    private final CategoryBonusRepository categoryBonusRepository;
    private final HappyHourRepository happyHourRepository;
    private final ModelMapper modelMapper;
    
    private static final BigDecimal REWARD_RATE = new BigDecimal("0.03"); // 3% reward rate
    private static final BigDecimal POINTS_PER_DOLLAR = new BigDecimal("10"); // 10 points per $1
    private static final int POINTS_EXPIRY_MONTHS = 12; // Points expire after 12 months
    private static final BigDecimal MINIMUM_REDEMPTION = new BigDecimal("2.50"); // Minimum $2.50 redemption

    @Override
    public RewardPointDto getCustomerPoints(UUID customerId) {
        LoggingUtils.logMethodEntry(log, "getCustomerPoints", "customerId", customerId);
        
        RewardPoint rewardPoint = getOrCreateRewardPoint(customerId);
        return modelMapper.map(rewardPoint, RewardPointDto.class);
    }

    @Override
    public List<RewardTransactionDto> getCustomerTransactions(UUID customerId) {
        LoggingUtils.logMethodEntry(log, "getCustomerTransactions", "customerId", customerId);
        
        return rewardTransactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(transaction -> modelMapper.map(transaction, RewardTransactionDto.class))
                .toList();
    }

    @Transactional
    @Override
    public void awardPointsForOrder(Order order) {
        LoggingUtils.logMethodEntry(log, "awardPointsForOrder", "orderId", order.getId());
        
        BigDecimal baseReward = order.getTotalAmount().multiply(REWARD_RATE);
        BigDecimal totalReward = calculateBonusReward(baseReward, order);
        BigDecimal pointsEarned = totalReward.multiply(POINTS_PER_DOLLAR)
                .setScale(0, RoundingMode.HALF_UP);
        
        if (pointsEarned.compareTo(BigDecimal.ZERO) > 0) {
            addPoints(order.getCustomer().getId(), pointsEarned, 
                    RewardTransaction.TransactionType.EARNED_DELIVERY,
                    "Points earned from order #" + order.getId().toString().substring(0, 8),
                    order);
            
            LoggingUtils.logBusinessEvent(log, "POINTS_AWARDED", 
                    "customerId", order.getCustomer().getId(),
                    "orderId", order.getId(),
                    "pointsEarned", pointsEarned);
        }
    }

    @Transactional
    @Override
    public BigDecimal redeemPoints(UUID customerId, BigDecimal pointsToRedeem) {
        LoggingUtils.logMethodEntry(log, "redeemPoints", "customerId", customerId, "points", pointsToRedeem);
        
        if (pointsToRedeem.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Points to redeem must be positive");
        }
        
        BigDecimal dollarValue = pointsToRedeem.divide(POINTS_PER_DOLLAR, 2, RoundingMode.DOWN);
        if (dollarValue.compareTo(MINIMUM_REDEMPTION) < 0) {
            throw new BusinessRuleException("Minimum redemption is $" + MINIMUM_REDEMPTION + " (" + MINIMUM_REDEMPTION.multiply(POINTS_PER_DOLLAR).intValue() + " points)");
        }
        
        RewardPoint rewardPoint = getOrCreateRewardPoint(customerId);
        
        if (rewardPoint.getBalance().compareTo(pointsToRedeem) < 0) {
            throw new BusinessRuleException("Insufficient points balance");
        }
        
        // Deduct points
        rewardPoint.setBalance(rewardPoint.getBalance().subtract(pointsToRedeem));
        rewardPointRepository.save(rewardPoint);
        
        // Record transaction
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        
        RewardTransaction transaction = new RewardTransaction();
        transaction.setCustomer(customer);
        transaction.setType(RewardTransaction.TransactionType.USED_ORDER);
        transaction.setPoints(pointsToRedeem.negate());
        transaction.setDescription("Points redeemed for order discount");
        
        rewardTransactionRepository.save(transaction);
        
        // Convert points to dollars (10 points = $1)
        BigDecimal discountAmount = pointsToRedeem.divide(POINTS_PER_DOLLAR, 2, RoundingMode.DOWN);
        
        LoggingUtils.logBusinessEvent(log, "POINTS_REDEEMED",
                "customerId", customerId,
                "pointsRedeemed", pointsToRedeem,
                "discountAmount", discountAmount);
        
        return discountAmount;
    }

    @Transactional
    @Override
    public void creditPoints(UUID customerId, BigDecimal points, String description) {
        LoggingUtils.logMethodEntry(log, "creditPoints", "customerId", customerId, "points", points);
        
        if (points.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Points to credit must be positive");
        }
        
        addPoints(customerId, points, RewardTransaction.TransactionType.EARNED_PROMOTION, description, null);
        
        LoggingUtils.logBusinessEvent(log, "POINTS_CREDITED",
                "customerId", customerId,
                "pointsCredited", points);
    }

    @Transactional
    @Override
    public void compensateFailedOrder(Order order) {
        LoggingUtils.logMethodEntry(log, "compensateFailedOrder", "orderId", order.getId());
        
        BigDecimal compensationPoints = order.getTotalAmount().multiply(POINTS_PER_DOLLAR)
                .setScale(0, RoundingMode.HALF_UP); // Full refund as points
        
        addPoints(order.getCustomer().getId(), compensationPoints,
                RewardTransaction.TransactionType.EARNED_COMPENSATION,
                "Compensation for cancelled order #" + order.getId().toString().substring(0, 8),
                order);
        
        LoggingUtils.logBusinessEvent(log, "ORDER_COMPENSATED",
                "customerId", order.getCustomer().getId(),
                "orderId", order.getId(),
                "compensationPoints", compensationPoints);
    }

    private RewardPoint getOrCreateRewardPoint(UUID customerId) {
        return rewardPointRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Customer customer = customerRepository.findById(customerId)
                            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
                    
                    RewardPoint newRewardPoint = new RewardPoint();
                    newRewardPoint.setCustomer(customer);
                    newRewardPoint.setBalance(BigDecimal.ZERO);
                    
                    return rewardPointRepository.save(newRewardPoint);
                });
    }

    @Override
    public void processDeliveryReward(Order order) {
        if (order.getStatus() == OrderStatus.DELIVERED) {
            awardPointsForOrder(order);
        }
    }

    @Override
    public void processRefundReward(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            compensateFailedOrder(order);
        }
    }

    @Override
    public void addCompensationReward(UUID customerId, BigDecimal points, String description) {
        creditPoints(customerId, points, description);
    }

    @Override
    public void addPromotionReward(UUID customerId, BigDecimal points, String description) {
        creditPoints(customerId, points, description);
    }

    @Override
    @Transactional
    public void addPromotionRewardToAll(BigDecimal points, String description) {
        List<Customer> allCustomers = customerRepository.findAll();
        for (Customer customer : allCustomers) {
            creditPoints(customer.getId(), points, description);
        }
    }

    @Override
    public BigDecimal useRewardPoints(UUID customerId, BigDecimal pointsToUse, Order order) {
        return redeemPoints(customerId, pointsToUse);
    }

    @Override
    public BigDecimal getRewardBalance(UUID customerId) {
        RewardPoint rewardPoint = getOrCreateRewardPoint(customerId);
        return rewardPoint.getBalance();
    }

    @Override
    public List<RewardTransaction> getRewardHistory(UUID customerId) {
        return rewardTransactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    private BigDecimal calculateBonusReward(BigDecimal baseReward, Order order) {
        BigDecimal totalReward = baseReward;
        LocalDateTime now = LocalDateTime.now();
        
        // Check for happy hour bonus
        Optional<HappyHour> happyHour = happyHourRepository.findActiveHappyHour(now);
        if (happyHour.isPresent()) {
            BigDecimal happyHourBonus = order.getTotalAmount().multiply(happyHour.get().getBonusRate());
            totalReward = totalReward.add(happyHourBonus);
        }
        
        // Check for category bonus
        if (order.getRestaurant() != null && order.getRestaurant().getCategories() != null) {
            for (RestaurantCategory category : order.getRestaurant().getCategories()) {
                Optional<CategoryBonus> categoryBonus = categoryBonusRepository
                    .findActiveBonusForCategory(category.getName(), now);
                if (categoryBonus.isPresent()) {
                    BigDecimal categoryBonusAmount = order.getTotalAmount().multiply(categoryBonus.get().getBonusRate());
                    totalReward = totalReward.add(categoryBonusAmount);
                    break; // Only apply one category bonus
                }
            }
        }
        
        return totalReward;
    }

    private void addPoints(UUID customerId, BigDecimal points, 
                          RewardTransaction.TransactionType type, String description, Order order) {
        RewardPoint rewardPoint = getOrCreateRewardPoint(customerId);
        rewardPoint.setBalance(rewardPoint.getBalance().add(points));
        rewardPointRepository.save(rewardPoint);
        
        RewardTransaction transaction = new RewardTransaction();
        transaction.setCustomer(rewardPoint.getCustomer());
        transaction.setType(type);
        transaction.setPoints(points);
        transaction.setDescription(description);
        transaction.setRelatedOrder(order);
        
        rewardTransactionRepository.save(transaction);
    }
}