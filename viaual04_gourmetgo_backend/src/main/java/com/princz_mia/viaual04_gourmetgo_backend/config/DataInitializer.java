package com.princz_mia.viaual04_gourmetgo_backend.config;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.*;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final CredentialRepository credentialRepository;
    private final CartRepository cartRepository;
    private final RewardPointRepository rewardPointRepository;
    private final RewardTransactionRepository rewardTransactionRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setEmailAddress("admin@gourmetgo.com");
            admin.setFullName("System Administrator");
            admin.setCreatedAt(LocalDateTime.now());
            admin.setLoginAttempts(0);
            admin.setAccountNonLocked(true);
            admin.setEnabled(true);
            admin.setDeleted(false);
            
            admin = adminRepository.save(admin);
            
            Credential credential = new Credential();
            credential.setUser(admin);
            credential.setPassword(passwordEncoder.encode("admin123"));
            credentialRepository.save(credential);
            
            System.out.println("Admin user created: admin@gourmetgo.com / admin123");
        }

        if (customerRepository.count() == 0) {
            Customer customer = new Customer();
            customer.setEmailAddress("princz.mia@gmail.com");
            customer.setFullName("Mia Princz");
            customer.setPhoneNumber("+36301234567");
            customer.setCreatedAt(LocalDateTime.now());
            customer.setLoginAttempts(0);
            customer.setAccountNonLocked(true);
            customer.setEnabled(true);
            customer.setDeleted(false);

            customer = customerRepository.save(customer);

            Credential credential = new Credential();
            credential.setUser(customer);
            credential.setPassword(passwordEncoder.encode("testtest"));
            credentialRepository.save(credential);

            Cart cart = Cart.builder()
                    .customer(customer)
                    .build();
            cartRepository.save(cart);

            RewardPoint rewardPoint = new RewardPoint();
            rewardPoint.setCustomer(customer);
            rewardPoint.setBalance(new java.math.BigDecimal("100.00"));
            rewardPointRepository.save(rewardPoint);

            RewardTransaction transaction = new RewardTransaction();
            transaction.setCustomer(customer);
            transaction.setPoints(new java.math.BigDecimal("100.00"));
            transaction.setType(RewardTransaction.TransactionType.EARNED_PROMOTION);
            transaction.setDescription("Welcome bonus");
            rewardTransactionRepository.save(transaction);

            System.out.println("Customer created: princz.mia@gmail.com / testtest");
        }

        if (restaurantRepository.count() == 0) {
            RestaurantCategory fastFood = RestaurantCategory.builder()
                    .name("Fast Food")
                    .build();
            restaurantCategoryRepository.save(fastFood);

            Address address = Address.builder()
                    .addressLine("Lágymányosi út 15")
                    .city("Budapest")
                    .postalCode("1111")
                    .region("Budapest")
                    .build();
            addressRepository.save(address);

            Restaurant restaurant = new Restaurant();
            restaurant.setEmailAddress("restaurant@gourmetgo.com");
            restaurant.setFullName("Lágymányosi Bistro");
            restaurant.setPhoneNumber("+36301234568");
            restaurant.setDeliveryFee(new java.math.BigDecimal("2.50"));
            restaurant.setApproved(true);
            restaurant.setAddress(address);
            restaurant.setLatitude(47.4736);
            restaurant.setLongitude(19.0511);
            restaurant.getCategories().add(fastFood);
            restaurant.getOpeningHours().put(java.time.DayOfWeek.MONDAY, new Restaurant.Hours(java.time.LocalTime.of(9, 0), java.time.LocalTime.of(22, 0)));
            restaurant.getOpeningHours().put(java.time.DayOfWeek.TUESDAY, new Restaurant.Hours(java.time.LocalTime.of(9, 0), java.time.LocalTime.of(22, 0)));
            restaurant.getOpeningHours().put(java.time.DayOfWeek.WEDNESDAY, new Restaurant.Hours(java.time.LocalTime.of(9, 0), java.time.LocalTime.of(22, 0)));
            restaurant.getOpeningHours().put(java.time.DayOfWeek.THURSDAY, new Restaurant.Hours(java.time.LocalTime.of(9, 0), java.time.LocalTime.of(22, 0)));
            restaurant.getOpeningHours().put(java.time.DayOfWeek.FRIDAY, new Restaurant.Hours(java.time.LocalTime.of(9, 0), java.time.LocalTime.of(23, 0)));
            restaurant.getOpeningHours().put(java.time.DayOfWeek.SATURDAY, new Restaurant.Hours(java.time.LocalTime.of(10, 0), java.time.LocalTime.of(23, 0)));
            restaurant.getOpeningHours().put(java.time.DayOfWeek.SUNDAY, new Restaurant.Hours(java.time.LocalTime.of(10, 0), java.time.LocalTime.of(21, 0)));
            restaurant.setCreatedAt(LocalDateTime.now());
            restaurant.setLoginAttempts(0);
            restaurant.setAccountNonLocked(true);
            restaurant.setEnabled(true);
            restaurant.setDeleted(false);
            restaurant = restaurantRepository.save(restaurant);

            Credential restaurantCredential = new Credential();
            restaurantCredential.setUser(restaurant);
            restaurantCredential.setPassword(passwordEncoder.encode("restaurant123"));
            credentialRepository.save(restaurantCredential);

            ProductCategory pizzaCategory = ProductCategory.builder()
                    .name("Pizza")
                    .restaurant(restaurant)
                    .build();
            productCategoryRepository.save(pizzaCategory);

            Product margherita = Product.builder()
                    .name("Margherita Pizza")
                    .description("Classic pizza with tomato, mozzarella, and basil")
                    .price(new java.math.BigDecimal("12.99"))
                    .inventory(50)
                    .category(pizzaCategory)
                    .restaurant(restaurant)
                    .build();
            productRepository.save(margherita);

            System.out.println("Restaurant created: restaurant@gourmetgo.com / restaurant123");
        }

        if (paymentMethodRepository.count() == 0) {
            PaymentMethod creditCard = PaymentMethod.builder().name("Credit Card").build();
            PaymentMethod paypal = PaymentMethod.builder().name("PayPal").build();
            PaymentMethod cash = PaymentMethod.builder().name("Cash on Delivery").build();
            
            paymentMethodRepository.save(creditCard);
            paymentMethodRepository.save(paypal);
            paymentMethodRepository.save(cash);
            
            System.out.println("Payment methods created");
        }
    }
}