package com.princz_mia.viaual04_gourmetgo_backend.image;

import com.princz_mia.viaual04_gourmetgo_backend.product.Product;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant.Restaurant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Blob;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "images",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "product_id"),
                @UniqueConstraint(columnNames = "restaurant_id")
        }
)
public class Image {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    private String fileName;
    private String fileType;
    private String downloadUrl;

    @Lob
    private Blob data;

    @OneToOne
    @JoinColumn(name = "product_id", unique = true)
    private Product product;

    @OneToOne
    @JoinColumn(name = "restaurant_id", unique = true)
    private Restaurant restaurant;
}