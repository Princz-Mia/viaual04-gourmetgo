package com.princz_mia.viaual04_gourmetgo_backend.events;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Admin;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.email.EmailService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Restaurant;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final EmailService emailService;

    @EventListener
    public void onUserEvent(UserEvent event) {
        User user = event.getUser();

        if (user instanceof Customer customer) {
            switch (event.getType()) {
                case REGISTRATION -> emailService.sendNewAccountEmail(
                        customer.getFullName(),
                        customer.getEmailAddress(),
                        event.getData().get("key").toString()
                );
                case RESET_PASSWORD -> emailService.sendPasswordResetEmail(
                        customer.getFullName(),
                        customer.getEmailAddress(),
                        event.getData().get("key").toString()
                );
                default -> {
                }
            }
        } else if (user instanceof Restaurant restaurant) {
            switch (event.getType()) {
                case REGISTRATION -> emailService.sendRestaurantRegistrationEmail(
                        restaurant.getOwnerName(),
                        restaurant.getEmailAddress()
                );
                case APPROVED -> emailService.sendRestaurantApprovedEmail(
                        restaurant.getOwnerName(),
                        restaurant.getEmailAddress(),
                        event.getData().get("key").toString()
                );
                case RESET_PASSWORD -> emailService.sendPasswordResetEmail(
                        restaurant.getOwnerName(),
                        restaurant.getEmailAddress(),
                        event.getData().get("key").toString()
                );
                default -> {
                }
            }
        } else if (user instanceof Admin admin) {
            switch (event.getType()) {
                case RESET_PASSWORD -> emailService.sendPasswordResetEmail(
                        admin.getFullName(),
                        admin.getEmailAddress(),
                        event.getData().get("key").toString()
                );
                default -> {
                }
            }
        }
    }
}
