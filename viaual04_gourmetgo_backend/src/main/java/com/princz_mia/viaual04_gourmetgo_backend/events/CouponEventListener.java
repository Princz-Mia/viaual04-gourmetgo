package com.princz_mia.viaual04_gourmetgo_backend.events;

import com.princz_mia.viaual04_gourmetgo_backend.customer.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.customer.ICustomerService;
import com.princz_mia.viaual04_gourmetgo_backend.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponEventListener {

    private final EmailService emailService;
    private final ICustomerService customerService;

    @EventListener
    public void onCouponEvent(CouponEvent event) {
        switch (event.getType()) {
            case COUPON_PUBLISHED -> {
                List<Customer> customers = customerService.getAllEnabledAndNonLockedCustomer();
                for (Customer customer : customers) {
                    emailService.sendNewCouponPublishedEmail(customer.getFullName(), customer.getEmailAddress(), event.getCoupon());
                }
            }
            default -> {
            }
        }
    }
}
