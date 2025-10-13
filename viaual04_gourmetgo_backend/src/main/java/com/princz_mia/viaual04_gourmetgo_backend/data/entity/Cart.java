package com.princz_mia.viaual04_gourmetgo_backend.cart;

import com.princz_mia.viaual04_gourmetgo_backend.cart_item.CartItem;
import com.princz_mia.viaual04_gourmetgo_backend.customer.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entity representing a customer's shopping cart in the GourmetGo application.
 * <p>
 * Contains cart items and dynamically calculates the total amount.
 * Each customer can have exactly one cart.
 * </p>
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    /**
     * Unique identifier for the cart
     * Automatically generated using a random UUID strategy.
     */
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    /**
     * Total amount of all items in the cart.
     * Automatically updated when items are added or removed.
     */
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * Items currently in the cart.
     * The cart owns the items and they are removed if the cart is deleted.
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> items = new HashSet<>();

    /**
     * The customer who owns this cart.
     * This association is required and fetched eagerly.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    /**
     * Adds an item to the cart and updates the total amount.
     * @param item the item to be added
     */
    public void addItem(CartItem item) {
        this.items.add(item);
        item.setCart(this);
        updateTotalAmount();
    }

    /**
     * Removes an item from the cart and updates the total amount.
     * @param item the item to be removed
     */
    public void removeItem(CartItem item) {
        this.items.remove(item);
        item.setCart(null);
        updateTotalAmount();
    }

    /**
     * Recalculates the total amount based on current cart items.
     * Handles null unit prices safely.
     */
    private void updateTotalAmount() {
        this.totalAmount = items.stream().map(item -> {
            BigDecimal unitPrice = item.getUnitPrice();
            if (unitPrice == null) {
                return BigDecimal.ZERO;
            }
            return unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
