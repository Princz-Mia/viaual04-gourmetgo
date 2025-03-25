package com.princz_mia.viaual04_gourmetgo_backend.customer;

import com.princz_mia.viaual04_gourmetgo_backend.cart.Cart;
import com.princz_mia.viaual04_gourmetgo_backend.order.Order;
import com.princz_mia.viaual04_gourmetgo_backend.user.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Entity representing a customer in the GourmetGo application.
 * <p>
 * Extends the base {@link User} entity by adding personal information and relations
 * to the customer's shopping cart and orders.
 * </p>
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Customer extends User {

    /**
     * First name of the customer.
     * Cannot be empty.
     */
    @NotEmpty(message = "First name cannot be empty")
    private String firstName;

    /**
     * Last name of the customer.
     * Cannot be empty.
     */
    @NotEmpty(message = "Last name cannot be empty")
    private String lastName;

    /**
     * Optional phone number of the customer.
     */
    private String phoneNumber;

    /**
     * One-to-one relation to the customer's cart.
     * <p>
     * The cart is owned by the customer and is automatically removed when the customer is deleted.
     * </p>
     */
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    /**
     * One-to-many relation to the customer's orders.
     * <p>
     * Each customer may have multiple orders. All orders are removed when the customer is deleted.
     * </p>
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;
}
