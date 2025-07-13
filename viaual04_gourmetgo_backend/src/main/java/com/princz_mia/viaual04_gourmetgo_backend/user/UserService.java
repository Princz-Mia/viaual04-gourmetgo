package com.princz_mia.viaual04_gourmetgo_backend.user;

import com.princz_mia.viaual04_gourmetgo_backend.admin.Admin;
import com.princz_mia.viaual04_gourmetgo_backend.admin.AdminRepository;
import com.princz_mia.viaual04_gourmetgo_backend.config.security.PasswordDto;
import com.princz_mia.viaual04_gourmetgo_backend.confirmation.Confirmation;
import com.princz_mia.viaual04_gourmetgo_backend.confirmation.ConfirmationRepository;
import com.princz_mia.viaual04_gourmetgo_backend.credential.Credential;
import com.princz_mia.viaual04_gourmetgo_backend.credential.CredentialRepository;
import com.princz_mia.viaual04_gourmetgo_backend.customer.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.customer.CustomerRepository;
import com.princz_mia.viaual04_gourmetgo_backend.events.EventType;
import com.princz_mia.viaual04_gourmetgo_backend.events.UserEvent;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant.Restaurant;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ConfirmationRepository confirmationRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final CredentialRepository credentialRepository;
    private final ModelMapper modelMapper;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final AdminRepository adminRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertUserToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void lockUser(UUID userId, boolean locked) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found", HttpStatus.NOT_FOUND));
        // locked=true => accountNonLocked=false
        u.setAccountNonLocked(!locked);
        userRepository.save(u);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User was not found", HttpStatus.NOT_FOUND));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserDto updateProfile(User user, ProfileUpdateDto dto) {
        User u = Optional.of(userRepository.findByEmailAddress(user.getEmailAddress()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found", HttpStatus.NOT_FOUND));

        if (!u.getEmailAddress().equals(dto.getEmailAddress())
                && userRepository.existsByEmailAddress(dto.getEmailAddress())) {
            throw new AppException("Email address already in use", HttpStatus.BAD_REQUEST);
        }

        u.setEmailAddress(dto.getEmailAddress());

        if (u instanceof Customer) {
            Customer c = (Customer) u;
            c.setFullName(dto.getFullName());
            customerRepository.save(c);
            u = c; // <-- erre fontos!
        } else if (u instanceof Restaurant) {
            Restaurant r = (Restaurant) u;
            r.setOwnerName(dto.getFullName());
            restaurantRepository.save(r);
            u = r;
        } else if (u instanceof Admin) {
            Admin a = (Admin) u;
            a.setFullName(dto.getFullName());
            adminRepository.save(a);
            u = a;
        }

        // Save the updated base user entity, if necessary
        userRepository.save(u);

        // Map updated user to DTO
        UserDto userDto = modelMapper.map(u, UserDto.class);
        userDto.setFullName(dto.getFullName()); // biztos, ami biztos
        return userDto;
    }


    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        User user = Optional.of(userRepository.findByEmailAddress(email))
                .orElseThrow(() ->
                        new ResourceNotFoundException("No user found with email: " + email, HttpStatus.NOT_FOUND));

        Confirmation confirmation = new Confirmation(user);
        confirmationRepository.save(confirmation);

        eventPublisher.publishEvent(new UserEvent(user, EventType.RESET_PASSWORD, Map.of("key", confirmation.getKey())));
    }

    @Override
    @Transactional
    public void resetPassword(String key, PasswordDto passwordDto) {
        if (!passwordDto.getPassword().equals(passwordDto.getConfirmPassword())) {
            throw new AppException("Passwords are not matching", HttpStatus.BAD_REQUEST);
        }

        Confirmation confirmation = getUserConfirmation(key);
        User user = userRepository.findByEmailAddress(confirmation.getUser().getEmailAddress());

        String encoded = bCryptPasswordEncoder.encode(passwordDto.getPassword());

        Credential cred = credentialRepository
                .findByUser(user)
                .orElseGet(() -> new Credential(user, null));

        cred.setPassword(encoded);
        credentialRepository.save(cred);

        confirmationRepository.delete(confirmation);
    }


    @Override
    public User getAuthenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailAddress = authentication.getName();
        return userRepository.findByEmailAddress(emailAddress);
    }

    private Confirmation getUserConfirmation(String key) {
        return Optional.of(confirmationRepository.findByKey(key))
                .orElseThrow(() -> new AppException("Confirmation key was not found in database", HttpStatus.NOT_FOUND));
    }

    private UserDto convertUserToDto(User user) {
        String role;
        if (user instanceof Admin) role = "ROLE_ADMIN";
        else if (user instanceof Restaurant) role = "ROLE_RESTAURANT";
        else if (user instanceof Customer) role = "ROLE_CUSTOMER";
        else role = "ROLE_UNKNOWN";

        return UserDto.builder()
                .id(user.getId())
                .fullName(
                        user instanceof Admin       ? ((Admin) user).getFullName() :
                                user instanceof Customer    ? ((Customer) user).getFullName() :
                                        user instanceof Restaurant  ? ((Restaurant) user).getOwnerName() :
                                                ""
                )
                .emailAddress(user.getEmailAddress())
                .role(role)
                .isAccountNonLocked(user.isAccountNonLocked())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}