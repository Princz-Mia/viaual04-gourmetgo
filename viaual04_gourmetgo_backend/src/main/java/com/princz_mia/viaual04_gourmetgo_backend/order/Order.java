package com.princz_mia.viaual04_gourmetgo_backend.order;

import com.princz_mia.viaual04_gourmetgo_backend.customer.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.order_item.OrderItem;
import com.princz_mia.viaual04_gourmetgo_backend.order_status.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    /**
     * Unique identifier for the order.
     * Automatically generated using a random UUID strategy.
     */
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    /**
     * Date when the order was placed.
     */
    private LocalDate orderDate;

    /**
     * Total amount for the entire order.
     */
    private BigDecimal totalAmount;

    /**
     * Current status of the order (e.g., PENDING, CONFIRMED, DELIVERED).
     */
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    /**
     * List of items included in the order.
     * Cascade operations ensure items are persisted/removed with the order.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> orderItems = new HashSet<>();

    /**
     * The customer who placed the order.
     */
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
