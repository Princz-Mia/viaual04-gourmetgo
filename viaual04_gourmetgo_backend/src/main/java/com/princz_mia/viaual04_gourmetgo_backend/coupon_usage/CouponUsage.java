package com.princz_mia.viaual04_gourmetgo_backend.coupon_usage;

import com.princz_mia.viaual04_gourmetgo_backend.coupon.Coupon;
import com.princz_mia.viaual04_gourmetgo_backend.customer.Customer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "coupon_usage",
        uniqueConstraints = @UniqueConstraint(columnNames = {"coupon_id","customer_id"})
)
public class CouponUsage {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @CreationTimestamp
    private LocalDateTime usedAt;
}
