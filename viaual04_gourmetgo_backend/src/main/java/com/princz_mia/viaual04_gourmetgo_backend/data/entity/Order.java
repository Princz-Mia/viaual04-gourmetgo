package com.princz_mia.viaual04_gourmetgo_backend.order;

import com.princz_mia.viaual04_gourmetgo_backend.address.Address;
import com.princz_mia.viaual04_gourmetgo_backend.coupon.Coupon;
import com.princz_mia.viaual04_gourmetgo_backend.customer.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.order_item.OrderItem;
import com.princz_mia.viaual04_gourmetgo_backend.order_status.OrderStatus;
import com.princz_mia.viaual04_gourmetgo_backend.payment_method.PaymentMethod;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant.Restaurant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entity representing a customer's order in the GourmetGo application.
 * <p>
 * Stores order date, total amount, current status, and links to customer and ordered items.
 * </p>
 */
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private String orderNotes;
    private String deliveryInstructions;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> orderItems = new HashSet<>();

    @ManyToOne
    private PaymentMethod paymentMethod;

    @ManyToOne
    private Coupon coupon;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // Billing info as @Embeddable, de Address-asszociációval:
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="fullName",      column=@Column(name="billing_full_name")),
            @AttributeOverride(name="phoneNumber",   column=@Column(name="billing_phone")),
            // az address mező beágyazott entitás, ezért nincs override a primitive mezőkre
    })
    private BillingInformation billingInformation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="fullName",      column=@Column(name="shipping_full_name")),
            @AttributeOverride(name="phoneNumber",   column=@Column(name="shipping_phone")),
    })
    private ShippingInformation shippingInformation;


    @Getter @Setter
    @Embeddable
    public static class BillingInformation {
        private String fullName;
        private String phoneNumber;

        @ManyToOne
        @JoinColumn(name = "billing_address_id", nullable = false)
        private Address address;
    }

    @Getter @Setter
    @Embeddable
    public static class ShippingInformation {
        private String fullName;
        private String phoneNumber;

        @ManyToOne
        @JoinColumn(name = "shipping_address_id", nullable = false)
        private Address address;
    }
}
