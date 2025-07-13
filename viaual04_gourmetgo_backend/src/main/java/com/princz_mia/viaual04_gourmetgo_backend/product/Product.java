package com.princz_mia.viaual04_gourmetgo_backend.product;

import com.princz_mia.viaual04_gourmetgo_backend.product_category.ProductCategory;
import com.princz_mia.viaual04_gourmetgo_backend.image.Image;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant.Restaurant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing a product in the GourmetGo application.
 * <p>
 * Products are items listed in the menu of a restaurant, such as meals or drinks.
 * Each product belongs to a category, is linked to a restaurant, and may have an image.
 * </p>
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE product SET deleted = true WHERE id = ?")
public class Product {

    /**
     * Unique identifier of the product.
     * Automatically generated using a random UUID strategy.
     */
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    /**
     * Name of the product.
     */
    private String name;

    /**
     * Optional description of the product, providing more details.
     */
    private String description;

    /**
     * Price of the product in the local currency.
     */
    private BigDecimal price;

    /**
     * Current inventory level for the product.
     */
    private int inventory;

    /**
     * Category to which the product belongs (e.g., "Pizza", "Dessert").
     * Cascade is enabled to propagate changes.
     */
    @ManyToOne
    @JoinColumn(name = "product_category_id")
    private ProductCategory category;

    /**
     * Restaurant that offers this product.
     * This association is mandatory.
     */
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    /**
     * One-to-one relation to the product's image.
     * Automatically removed if the product is deleted.
     */
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Image image;

    @Column(nullable = false)
    private boolean deleted = false;
}
