package com.princz_mia.viaual04_gourmetgo_backend.restaurant;

import com.princz_mia.viaual04_gourmetgo_backend.address.AddressDto;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRegistrationDto {
    private String name;
    private String phoneNumber;
    @Email
    private String emailAddress;
    private String ownerName;
    private BigDecimal deliveryFee;
    private List<String> categoryNames;
    private AddressDto address;
    private Map<DayOfWeek, RestaurantDto.HoursDto> openingHours;
}