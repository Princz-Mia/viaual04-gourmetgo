package com.princz_mia.viaual04_gourmetgo_backend.order_item;

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
public class OrderItemDto {
    private UUID id;
    private ProductDto product;
    private int quantity;
    private BigDecimal price;
}
