package com.princz_mia.viaual04_gourmetgo_backend.product_category;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.princz_mia.viaual04_gourmetgo_backend.product.Product;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant.Restaurant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

/**
 * Entity representing a product category in the GourmetGo application.
 * <p>
 * Categories group similar products (e.g., "Burgers", "Drinks") within a restaurant.
 * </p>
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCategory {

    /**
     * Unique identifier for the category.
     * Automatically generated UUID.
     */
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    /**
     * Name of the category (e.g., "Pizza", "Dessert").
     */
    private String name;

    /**
     * Restaurant that owns this product category.
     * This association is required.
     */
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    /**
     * List of products that belong to this category.
     * Marked with @JsonIgnore to avoid circular references during serialization.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<Product> products;
}
