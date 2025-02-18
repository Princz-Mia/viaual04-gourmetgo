package com.princz_mia.viaual04_gourmetgo_backend.restaurant_order_status;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantOrderStatus {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID id;
}
