package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.HappyHour;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.HappyHourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HappyHourNotificationService {
    
    private final HappyHourRepository happyHourRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    private HappyHour lastActiveHappyHour = null;
    
    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkHappyHourStatus() {
        try {
            Optional<HappyHour> currentHappyHour = happyHourRepository.findActiveHappyHour(LocalDateTime.now());
            
            // Check if status changed
            boolean statusChanged = false;
            if (lastActiveHappyHour == null && currentHappyHour.isPresent()) {
                statusChanged = true; // Happy hour started
            } else if (lastActiveHappyHour != null && currentHappyHour.isEmpty()) {
                statusChanged = true; // Happy hour ended
            } else if (lastActiveHappyHour != null && currentHappyHour.isPresent() 
                      && !lastActiveHappyHour.getId().equals(currentHappyHour.get().getId())) {
                statusChanged = true; // Different happy hour started
            }
            
            if (statusChanged) {
                broadcastHappyHourUpdate(currentHappyHour.orElse(null));
                lastActiveHappyHour = currentHappyHour.orElse(null);
            }
        } catch (Exception e) {
            log.warn("Failed to check happy hour status", e);
        }
    }
    
    public void broadcastHappyHourUpdate(HappyHour happyHour) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "HAPPY_HOUR_UPDATE");
        message.put("isActive", happyHour != null);
        message.put("happyHour", happyHour);
        
        messagingTemplate.convertAndSend("/topic/happy-hour", message);
        log.info("Broadcasted happy hour update: {}", happyHour != null ? "ACTIVE" : "INACTIVE");
    }
    
    public void notifyHappyHourCreated(HappyHour happyHour) {
        // Check if this new happy hour is currently active
        if (happyHour.isCurrentlyActive()) {
            broadcastHappyHourUpdate(happyHour);
            lastActiveHappyHour = happyHour;
        }
    }
}