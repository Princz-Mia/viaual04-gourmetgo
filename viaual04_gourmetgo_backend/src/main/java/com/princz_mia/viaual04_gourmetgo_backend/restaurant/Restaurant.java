package com.princz_mia.viaual04_gourmetgo_backend.restaurant;

import com.princz_mia.viaual04_gourmetgo_backend.address.Address;
import com.princz_mia.viaual04_gourmetgo_backend.product_category.ProductCategory;
import com.princz_mia.viaual04_gourmetgo_backend.image.Image;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant_category.RestaurantCategory;
import com.princz_mia.viaual04_gourmetgo_backend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Restaurant extends User {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Embeddable
    public static class Hours {
        @Column(name="opening_time")
        private LocalTime openingTime;

        @Column(name="closing_time")
        private LocalTime closingTime;
    }

    private String name;
    private String phoneNumber;
    private String ownerName;
    private BigDecimal deliveryFee;
    private boolean isApproved;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id", referencedColumnName = "id", unique = false)
    private Address address;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "restaurant_category_mapping",
            joinColumns = @JoinColumn(name = "restaurant_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<RestaurantCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCategory> productCategories = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "restaurant_opening_hours",
            joinColumns = @JoinColumn(name = "restaurant_id")
    )
    @MapKeyColumn(name = "day_of_week")
    private Map<DayOfWeek, Hours> openingHours = new EnumMap<>(DayOfWeek.class);

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "logo_id", unique = true)
    private Image logo;
}
