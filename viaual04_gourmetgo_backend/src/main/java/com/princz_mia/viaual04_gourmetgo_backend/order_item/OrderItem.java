package com.princz_mia.viaual04_gourmetgo_backend.order_item;

import com.princz_mia.viaual04_gourmetgo_backend.product.Product;
import com.princz_mia.viaual04_gourmetgo_backend.order.Order;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing a single item within an order.
 * <p>
 * Links a product to an order with the purchased quantity and price.
 * </p>
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    /**
     * Unique identifier for the order item.
     * Automatically generated using a random UUID strategy.
     */
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    /**
     * Quantity of the product ordered.
     */
    private int quantity;

    /**
     * Total price for this item (unit price * quantity).
     */
    private BigDecimal price;

    /**
     * The order this item belongs to.
     */
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    /**
     * The product that was ordered.
     */
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
