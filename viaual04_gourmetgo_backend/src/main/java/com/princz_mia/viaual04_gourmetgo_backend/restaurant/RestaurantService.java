package com.princz_mia.viaual04_gourmetgo_backend.restaurant;

import com.princz_mia.viaual04_gourmetgo_backend.address.Address;
import com.princz_mia.viaual04_gourmetgo_backend.address.AddressRepository;
import com.princz_mia.viaual04_gourmetgo_backend.config.security.PasswordDto;
import com.princz_mia.viaual04_gourmetgo_backend.confirmation.Confirmation;
import com.princz_mia.viaual04_gourmetgo_backend.confirmation.ConfirmationRepository;
import com.princz_mia.viaual04_gourmetgo_backend.credential.Credential;
import com.princz_mia.viaual04_gourmetgo_backend.credential.CredentialRepository;
import com.princz_mia.viaual04_gourmetgo_backend.customer.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.events.EventType;
import com.princz_mia.viaual04_gourmetgo_backend.events.UserEvent;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.image.IImageService;
import com.princz_mia.viaual04_gourmetgo_backend.image.Image;
import com.princz_mia.viaual04_gourmetgo_backend.image.ImageDto;
import com.princz_mia.viaual04_gourmetgo_backend.image.ImageRepository;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant_category.RestaurantCategory;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant_category.RestaurantCategoryRepository;
import com.princz_mia.viaual04_gourmetgo_backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
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
public class RestaurantService implements IRestaurantService {

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
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<RestaurantDto> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::convertRestaurantToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RestaurantDto registerRestaurant(RestaurantRegistrationDto reg, MultipartFile logo) {
        if (userRepository.existsByEmailAddress(reg.getEmailAddress())) {
            throw new AlreadyExistsException("There is already an account using this email", HttpStatus.CONFLICT);
        }

        // Persist Address
        Address address = Address.builder()
                .region(reg.getAddress().getRegion())
                .postalCode(reg.getAddress().getPostalCode())
                .city(reg.getAddress().getCity())
                .addressLine(reg.getAddress().getAddressLine())
                .unitNumber(reg.getAddress().getUnitNumber())
                .build();
        address = addressRepository.save(address);

        // Handle categories: existing or new
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

        // Build openingHours map
        Map<DayOfWeek, Restaurant.Hours> hoursMap = new EnumMap<>(DayOfWeek.class);
        reg.getOpeningHours().forEach((day, dto) -> {
            Restaurant.Hours h = new Restaurant.Hours();
            h.setOpeningTime(dto.getOpeningTime());
            h.setClosingTime(dto.getClosingTime());
            hoursMap.put(day, h);
        });

        // Create and save Restaurant
        Restaurant restaurant = new Restaurant();
        restaurant.setName(reg.getName());
        restaurant.setEmailAddress(reg.getEmailAddress());
        restaurant.setPhoneNumber(reg.getPhoneNumber());
        restaurant.setOwnerName(reg.getOwnerName());
        restaurant.setDeliveryFee(reg.getDeliveryFee());
        restaurant.setApproved(false);
        restaurant.setAccountNonLocked(true);
        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setAddress(address);
        restaurant.setCategories(categories);
        restaurant.setOpeningHours(hoursMap);
        restaurant = restaurantRepository.save(restaurant);

        // 1) elmented a képet, kapsz vissza egy DTO-t
        ImageDto imgDto = imageService.saveRestaurantImage(logo, restaurant.getId());
        // 2) lekéred az entitást a repositoryból
        Image imgEntity = imageRepository.findById(imgDto.getId())
                .orElseThrow(() -> new RuntimeException("Saved image not found"));
        restaurant.setLogo(imgEntity);
        restaurant = restaurantRepository.save(restaurant);

        Confirmation confirmation = new Confirmation(restaurant);
        confirmationRepository.save(confirmation);

        return modelMapper.map(restaurant, RestaurantDto.class);
    }

    @Override
    @Transactional
    public RestaurantDto convertRestaurantToDto(Restaurant restaurant) {
        return modelMapper.map(restaurant, RestaurantDto.class);
    }

    @Override
    public List<RestaurantDto> getPendingRestaurants() {
        return restaurantRepository.findByIsApproved(false)
                .stream()
                .map(this::convertRestaurantToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void approveRestaurant(UUID id) {
        Restaurant restaurant = getRestaurantById(id);
        restaurant.setApproved(true);
        restaurantRepository.save(restaurant);

        Confirmation confirmation = Optional.of(confirmationRepository.findByUser_Id(restaurant.getId()))
                        .orElseThrow(() -> new AppException("Confirmation key was not found in database", HttpStatus.NOT_FOUND));

        publisher.publishEvent(new UserEvent(restaurant, EventType.APPROVED, Map.of("key", confirmation.getKey())));
    }

    @Override
    public void rejectRestaurant(UUID id) {
        Restaurant restaurant = getRestaurantById(id);
        restaurantRepository.delete(restaurant);
    }

    @Override
    public void verifyAccountKey(String key, PasswordDto passwordDto) {
        if (passwordDto.getPassword().equals(passwordDto.getConfirmPassword())) {
            Confirmation confirmation = getCustomerConfirmation(key);
            Restaurant restaurant = getRestaurantByEmail(confirmation.getUser().getEmailAddress());
            restaurant.setEnabled(true);

            String encodedPassword = bCryptPasswordEncoder.encode(passwordDto.getPassword());
            Credential credential = new Credential(restaurant, encodedPassword);
            credentialRepository.save(credential);

            restaurantRepository.save(restaurant);
            confirmationRepository.delete(confirmation);
        } else {
            throw new AppException("Confirmation password is incorrect, passwords are not matching", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @Transactional
    public RestaurantDto updateRestaurant(UUID id, RestaurantDto dto) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found", HttpStatus.NOT_FOUND));

        // 1) deliveryFee validáció
        if (dto.getDeliveryFee() != null && dto.getDeliveryFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException("Delivery fee cannot be negative", HttpStatus.BAD_REQUEST);
        }

        // 2) egyszerű mezők frissítése
        r.setName(dto.getName());
        r.setPhoneNumber(dto.getPhoneNumber());
        r.setOwnerName(dto.getOwnerName());
        r.setDeliveryFee(dto.getDeliveryFee());

        // 3) Kategóriák: DTO-kból entitások
        List<RestaurantCategory> newCats = dto.getCategories().stream()
                .map(catDto -> {
                    if (catDto.getId() != null) {
                        return restaurantCategoryRepository.findById(catDto.getId())
                                .orElseThrow(() -> new AppException(
                                        "Category not found: " + catDto.getId(), HttpStatus.BAD_REQUEST));
                    } else {
                        // új kategória létrehozása
                        RestaurantCategory c = new RestaurantCategory();
                        c.setName(catDto.getName());
                        return restaurantCategoryRepository.save(c);
                    }
                })
                .collect(Collectors.toList());
        // Replace the old list
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
        return modelMapper.map(r, RestaurantDto.class);
    }

    private Confirmation getCustomerConfirmation(String key) {
        return Optional.of(confirmationRepository.findByKey(key))
                .orElseThrow(() -> new AppException("Confirmation key was not found in database", HttpStatus.NOT_FOUND));
    }

    private Restaurant getRestaurantByEmail(String emailAddress) {
        return Optional.of(restaurantRepository.findByEmailAddressIgnoreCase(emailAddress))
                .orElseThrow(() -> new AppException("Customer is not found with matching Email address", HttpStatus.NOT_FOUND));
    }
}
