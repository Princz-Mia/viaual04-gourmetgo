package com.princz_mia.viaual04_gourmetgo_backend.web.controller;


import com.princz_mia.viaual04_gourmetgo_backend.data.entity.CategoryBonus;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.HappyHour;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.CategoryBonusRepository;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.HappyHourRepository;
import com.princz_mia.viaual04_gourmetgo_backend.events.HappyHourEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final CategoryBonusRepository categoryBonusRepository;
    private final HappyHourRepository happyHourRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final com.princz_mia.viaual04_gourmetgo_backend.business.service.impl.HappyHourNotificationService notificationService;

    @GetMapping("/happy-hour/active")
    public ResponseEntity<HappyHour> getActiveHappyHour() {
        Optional<HappyHour> happyHour = happyHourRepository.findActiveHappyHour(LocalDateTime.now());
        return ResponseEntity.ok(happyHour.orElse(null));
    }

    @GetMapping("/category-bonuses/active")
    public ResponseEntity<List<CategoryBonus>> getActiveCategoryBonuses() {
        List<CategoryBonus> bonuses = categoryBonusRepository.findAllActiveBonuses(LocalDateTime.now());
        return ResponseEntity.ok(bonuses);
    }

    @PostMapping("/happy-hour")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HappyHour> createHappyHour(@RequestBody HappyHour happyHour) {
        HappyHour saved = happyHourRepository.save(happyHour);
        
        // Publish event for real-time updates
        eventPublisher.publishEvent(new HappyHourEvent("CREATE", saved.getId()));
        
        // Notify immediately if this happy hour is currently active
        notificationService.notifyHappyHourCreated(saved);
        
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/category-bonus")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryBonus> createCategoryBonus(@RequestBody CategoryBonus categoryBonus) {
        CategoryBonus saved = categoryBonusRepository.save(categoryBonus);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/happy-hours")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HappyHour>> getAllHappyHours() {
        return ResponseEntity.ok(happyHourRepository.findAll());
    }

    @DeleteMapping("/happy-hour/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHappyHour(@PathVariable Long id) {
        happyHourRepository.deleteById(id);
        
        // Publish event for real-time updates
        eventPublisher.publishEvent(new HappyHourEvent("DELETE", id));
        
        // Notify immediately that happy hour was deleted
        notificationService.broadcastHappyHourUpdate(null);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/category-bonuses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryBonus>> getAllCategoryBonuses() {
        return ResponseEntity.ok(categoryBonusRepository.findAll());
    }
    

}