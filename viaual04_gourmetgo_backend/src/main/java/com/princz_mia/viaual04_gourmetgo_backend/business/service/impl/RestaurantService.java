package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IImageService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.IRestaurantService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.*;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.*;
import com.princz_mia.viaual04_gourmetgo_backend.events.EventType;
import com.princz_mia.viaual04_gourmetgo_backend.events.UserEvent;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ErrorType;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ImageDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.PasswordDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantRegistrationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService implements IRestaurantService
{

    private final RestaurantRepository restaurantRepository;
    private final AddressRepository addressRepository;
    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final ImageRepository imageRepository;
    private final ConfirmationRepository confirmationRepository;

    private final IImageService imageService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ApplicationEventPublisher publisher;
    private final ModelMapper modelMapper;
    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Restaurant getRestaurantById(UUID id) {
        LoggingUtils.logMethodEntry(log, "getRestaurantById", "id", id);
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    }

    @Override
    public List<RestaurantDto> getAllRestaurants() {
        LoggingUtils.logMethodEntry(log, "getAllRestaurants");
        List<RestaurantDto> restaurants = restaurantRepository.findAll().stream()
                .map(this::convertRestaurantToDto)
                .collect(Collectors.toList());
        LoggingUtils.logBusinessEvent(log, "RESTAURANTS_RETRIEVED", "count", restaurants.size());
        return restaurants;
    }

    @Override
    @Transactional
    public RestaurantDto registerRestaurant(RestaurantRegistrationDto reg, MultipartFile logo) {
        LoggingUtils.logMethodEntry(log, "registerRestaurant", "name", reg.getName(), "email", LoggingUtils.maskSensitiveData(reg.getEmailAddress()));
        if (userRepository.existsByEmailAddress(reg.getEmailAddress())) {
            throw new AlreadyExistsException("There is already an account using this email");
        }

        Address address = Address.builder()
                .region(reg.getAddress().getRegion())
                .postalCode(reg.getAddress().getPostalCode())
                .city(reg.getAddress().getCity())
                .addressLine(reg.getAddress().getAddressLine())
                .unitNumber(reg.getAddress().getUnitNumber())
                .build();
        address = addressRepository.save(address);

        List<RestaurantCategory> categories = reg.getCategoryNames().stream()
                .map(name -> restaurantCategoryRepository
                        .findByName(name)
                        .orElseGet(() ->
                                restaurantCategoryRepository.save(
                                        RestaurantCategory.builder()
                                                .name(name)
                                                .build()
                                )
                        )
                )
                .collect(Collectors.toList());

        Map<DayOfWeek, Restaurant.Hours> hoursMap = new EnumMap<>(DayOfWeek.class);
        reg.getOpeningHours().forEach((day, dto) -> {
            Restaurant.Hours h = new Restaurant.Hours();
            h.setOpeningTime(dto.getOpeningTime());
            h.setClosingTime(dto.getClosingTime());
            hoursMap.put(day, h);
        });

        Restaurant restaurant = new Restaurant();
        restaurant.setFullName(reg.getName());
        restaurant.setEmailAddress(reg.getEmailAddress());
        restaurant.setPhoneNumber(reg.getPhoneNumber());
        restaurant.setDeliveryFee(reg.getDeliveryFee());
        restaurant.setApproved(false);
        restaurant.setAccountNonLocked(true);
        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setAddress(address);
        restaurant.setCategories(categories);
        restaurant.setOpeningHours(hoursMap);
        restaurant = restaurantRepository.save(restaurant);

        ImageDto imgDto = imageService.saveRestaurantImage(logo, restaurant.getId());
        Image imgEntity = imageRepository.findById(imgDto.getId())
                .orElseThrow(() -> new RuntimeException("Saved image not found"));
        restaurant.setLogo(imgEntity);
        restaurant = restaurantRepository.save(restaurant);

        Confirmation confirmation = new Confirmation(restaurant);
        confirmationRepository.save(confirmation);
        
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_REGISTERED", "restaurantId", restaurant.getId(), "name", restaurant.getFullName());
        return modelMapper.map(restaurant, RestaurantDto.class);
    }

    @Override
    @Transactional
    public RestaurantDto convertRestaurantToDto(Restaurant restaurant) {
        LoggingUtils.logMethodEntry(log, "convertRestaurantToDto", "restaurantId", restaurant.getId());
        RestaurantDto dto = modelMapper.map(restaurant, RestaurantDto.class);
        dto.setRating(restaurant.getRating());
        return dto;
    }

    @Override
    public List<RestaurantDto> getPendingRestaurants() {
        LoggingUtils.logMethodEntry(log, "getPendingRestaurants");
        List<RestaurantDto> pendingRestaurants = restaurantRepository.findByIsApproved(false)
                .stream()
                .map(this::convertRestaurantToDto)
                .collect(Collectors.toList());
        LoggingUtils.logBusinessEvent(log, "PENDING_RESTAURANTS_RETRIEVED", "count", pendingRestaurants.size());
        return pendingRestaurants;
    }

    @Override
    public void approveRestaurant(UUID id) {
        LoggingUtils.logMethodEntry(log, "approveRestaurant", "id", id);
        Restaurant restaurant = getRestaurantById(id);
        restaurant.setApproved(true);
        restaurantRepository.save(restaurant);

        Confirmation confirmation = Optional.of(confirmationRepository.findByUser_Id(restaurant.getId()))
                        .orElseThrow(() -> new AppException("Confirmation key was not found in database", ErrorType.RESOURCE_NOT_FOUND));

        publisher.publishEvent(new UserEvent(restaurant, EventType.APPROVED, Map.of("key", confirmation.getKey())));
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_APPROVED", "restaurantId", id, "name", restaurant.getFullName());
    }

    @Override
    public void rejectRestaurant(UUID id) {
        LoggingUtils.logMethodEntry(log, "rejectRestaurant", "id", id);
        Restaurant restaurant = getRestaurantById(id);
        restaurantRepository.delete(restaurant);
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_REJECTED", "restaurantId", id, "name", restaurant.getFullName());
    }

    @Override
    public void verifyAccountKey(String key, PasswordDto passwordDto) {
        LoggingUtils.logMethodEntry(log, "verifyAccountKey", "key", LoggingUtils.maskSensitiveData(key));
        if (passwordDto.getPassword().equals(passwordDto.getConfirmPassword())) {
            Confirmation confirmation = getCustomerConfirmation(key);
            Restaurant restaurant = getRestaurantByEmail(confirmation.getUser().getEmailAddress());
            restaurant.setEnabled(true);

            String encodedPassword = bCryptPasswordEncoder.encode(passwordDto.getPassword());
            Credential credential = new Credential(restaurant, encodedPassword);
            credentialRepository.save(credential);

            restaurantRepository.save(restaurant);
            confirmationRepository.delete(confirmation);
            LoggingUtils.logBusinessEvent(log, "RESTAURANT_ACCOUNT_VERIFIED", "restaurantId", restaurant.getId());
        } else {
            throw new AppException("Confirmation password is incorrect, passwords are not matching", ErrorType.VALIDATION_ERROR);
        }
    }

    @Override
    @Transactional
    public RestaurantDto updateRestaurant(UUID id, RestaurantDto dto) {
        LoggingUtils.logMethodEntry(log, "updateRestaurant", "id", id, "name", dto.getName());
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (dto.getDeliveryFee() != null && dto.getDeliveryFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException("Delivery fee cannot be negative", ErrorType.VALIDATION_ERROR);
        }

        r.setFullName(dto.getName());
        r.setPhoneNumber(dto.getPhoneNumber());
        r.setDeliveryFee(dto.getDeliveryFee());

        List<RestaurantCategory> newCats = dto.getCategories().stream()
                .map(catDto -> {
                    if (catDto.getId() != null) {
                        return restaurantCategoryRepository.findById(catDto.getId())
                                .orElseThrow(() -> new AppException(
                                        "Category not found: " + catDto.getId(), ErrorType.RESOURCE_NOT_FOUND));
                    } else {
                        RestaurantCategory c = new RestaurantCategory();
                        c.setName(catDto.getName());
                        return restaurantCategoryRepository.save(c);
                    }
                })
                .collect(Collectors.toList());
        r.getCategories().clear();
        r.getCategories().addAll(newCats);

        r.getOpeningHours().clear();
        restaurantRepository.save(r);

        Map<DayOfWeek, Restaurant.Hours> newHours = new EnumMap<>(DayOfWeek.class);
        dto.getOpeningHours().forEach((day, hoursDto) -> {
            Restaurant.Hours h = new Restaurant.Hours();
            h.setOpeningTime(hoursDto.getOpeningTime());
            h.setClosingTime(hoursDto.getClosingTime());
            newHours.put(day, h);
        });
        r.setOpeningHours(newHours);

        restaurantRepository.save(r);
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_UPDATED", "restaurantId", id, "name", r.getFullName());
        return modelMapper.map(r, RestaurantDto.class);
    }

    private Confirmation getCustomerConfirmation(String key) {
        return Optional.of(confirmationRepository.findByKey(key))
                .orElseThrow(() -> new AppException("Confirmation key was not found in database", ErrorType.RESOURCE_NOT_FOUND));
    }

    private Restaurant getRestaurantByEmail(String emailAddress) {
        return Optional.of(restaurantRepository.findByEmailAddressIgnoreCase(emailAddress))
                .orElseThrow(() -> new AppException("Customer is not found with matching Email address", ErrorType.RESOURCE_NOT_FOUND));
    }
}