package com.princz_mia.viaual04_gourmetgo_backend.cart_item;

import com.princz_mia.viaual04_gourmetgo_backend.cart.Cart;
import com.princz_mia.viaual04_gourmetgo_backend.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing an item in a shopping cart.
 * <p>
 * Each CartItem links a product with a specific quantity and price.
 * It also references the cart to which it belongs.
 * </p>
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {

    /**
     * Unique identifier for the cart item.
     * Automatically generated using a random UUID strategy.
     */
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    /**
     * Quantity of the product added to the cart.
     */
    private int quantity;

    /**
     * Price per unit of the product at the time of adding to cart.
     */
    private BigDecimal unitPrice;

    /**
     * Total price for this item (unit price * quantity).
     */
    private BigDecimal totalPrice;

    /**
     * The product associated with this cart item.
     */
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    /**
     * The cart this item belongs to.
     * Marked with @JsonIgnore to avoid circular references during serialization.
     */
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    /**
     * Calculates and sets the total price of this cart item.
     * Should be called after setting quantity and unit price.
     */
    public void setTotalPrice() {
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
