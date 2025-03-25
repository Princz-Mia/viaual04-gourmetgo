package com.princz_mia.viaual04_gourmetgo_backend.product_image;

import com.princz_mia.viaual04_gourmetgo_backend.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Blob;
import java.util.UUID;

/**
 * Entity representing an image associated with a product in the GourmetGo application.
 * <p>
 * Stores image metadata, download URL, and binary image data.
 * Each image is linked to exactly one product.
 * </p>
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductImage {

    /**
     * Unique identifier for the image.
     * Automatically generated UUID.
     */
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    /**
     * Original file name of the uploaded image.
     */
    private String fileName;

    /**
     * MIME type of the image (e.g., image/png, image/jpeg).
     */
    private String fileType;

    /**
     * Publicly accessible URL to download or view the image.
     */
    private String downloadUrl;

    /**
     * Binary content of the image stored as a BLOB in the database.
     */
    @Lob
    private Blob image;

    /**
     * One-to-one association to the related product.
     * Each product can have only one image.
     */
    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
