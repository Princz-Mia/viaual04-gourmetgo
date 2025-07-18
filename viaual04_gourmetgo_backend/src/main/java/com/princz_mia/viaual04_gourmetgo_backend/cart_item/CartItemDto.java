package com.princz_mia.viaual04_gourmetgo_backend.cart_item;

import com.princz_mia.viaual04_gourmetgo_backend.product.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private UUID id;
    private Integer quantity;
    private BigDecimal unitPrice;
    private ProductDto product;
}
