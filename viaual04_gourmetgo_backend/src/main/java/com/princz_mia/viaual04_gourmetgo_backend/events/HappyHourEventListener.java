package com.princz_mia.viaual04_gourmetgo_backend.events;

import com.princz_mia.viaual04_gourmetgo_backend.data.repository.HappyHourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HappyHourEventListener {
    
    private final HappyHourRepository happyHourRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    @EventListener
    public void handleHappyHourEvent(HappyHourEvent happyHourEvent) {
        try {
            Optional<com.princz_mia.viaual04_gourmetgo_backend.data.entity.HappyHour> activeHappyHour = 
                happyHourRepository.findActiveHappyHour(LocalDateTime.now());
            
            Map<String, Object> message = new HashMap<>();
            message.put("type", "HAPPY_HOUR_UPDATE");
            message.put("isActive", activeHappyHour.isPresent());
            message.put("happyHour", activeHappyHour.orElse(null));
            
            messagingTemplate.convertAndSend("/topic/happy-hour", message);
        } catch (Exception e) {
            // Ignore errors in event handling
        }
    }
}