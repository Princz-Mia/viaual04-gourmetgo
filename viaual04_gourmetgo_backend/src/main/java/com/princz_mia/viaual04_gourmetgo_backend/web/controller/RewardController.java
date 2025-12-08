package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IRewardService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.IOrderService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.RewardTransaction;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final IRewardService rewardService;
    private final IOrderService orderService;

    @GetMapping("/balance/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, BigDecimal>> getRewardBalance(@PathVariable java.util.UUID customerId) {
        BigDecimal balance = rewardService.getRewardBalance(customerId);
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    @GetMapping("/history/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<List<RewardTransaction>> getRewardHistory(@PathVariable java.util.UUID customerId) {
        List<RewardTransaction> history = rewardService.getRewardHistory(customerId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/compensation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addCompensationReward(
            @RequestParam java.util.UUID customerId,
            @RequestParam BigDecimal points,
            @RequestParam String description,
            @RequestParam java.util.UUID orderId) {
        rewardService.addCompensationReward(customerId, points, description);
        orderService.updateStatus(orderId, OrderStatus.COMPENSATED);
        return ResponseEntity.ok("Compensation reward added successfully");
    }

    @PostMapping("/promotion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addPromotionReward(
            @RequestParam java.util.UUID customerId,
            @RequestParam BigDecimal points,
            @RequestParam String description) {
        rewardService.addPromotionReward(customerId, points, description);
        return ResponseEntity.ok("Promotion reward added successfully");
    }

    @PostMapping("/promotion/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addPromotionRewardToAll(
            @RequestParam BigDecimal points,
            @RequestParam String description) {
        rewardService.addPromotionRewardToAll(points, description);
        return ResponseEntity.ok("Promotion reward sent to all customers successfully");
    }
}