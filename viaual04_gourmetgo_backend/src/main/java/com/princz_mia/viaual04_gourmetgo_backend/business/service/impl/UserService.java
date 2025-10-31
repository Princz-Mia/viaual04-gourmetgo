package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IUserService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.*;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.*;
import com.princz_mia.viaual04_gourmetgo_backend.events.EventType;
import com.princz_mia.viaual04_gourmetgo_backend.events.UserEvent;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ErrorType;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.PasswordDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ProfileUpdateDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

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
@Slf4j
public class UserService implements IUserService
{

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
        LoggingUtils.logMethodEntry(log, "getAllUsers");
        List<UserDto> users = userRepository.findAll().stream()
                .map(this::convertUserToDto)
                .collect(Collectors.toList());
        LoggingUtils.logBusinessEvent(log, "USERS_RETRIEVED", "count", users.size());
        return users;
    }

    @Override
    @Transactional
    public void lockUser(UUID userId, boolean locked) {
        LoggingUtils.logMethodEntry(log, "lockUser", "userId", userId, "locked", locked);
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found"));
        u.setAccountNonLocked(!locked);
        userRepository.save(u);
        LoggingUtils.logBusinessEvent(log, "USER_LOCK_STATUS_CHANGED", "userId", userId, "locked", locked);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        LoggingUtils.logMethodEntry(log, "deleteUser", "userId", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User was not found"));
        userRepository.delete(user);
        LoggingUtils.logBusinessEvent(log, "USER_DELETED", "userId", userId, "email", LoggingUtils.maskSensitiveData(user.getEmailAddress()));
    }

    @Override
    @Transactional
    public UserDto updateProfile(User user, ProfileUpdateDto dto) {
        LoggingUtils.logMethodEntry(log, "updateProfile", "userId", user.getId(), "email", LoggingUtils.maskSensitiveData(dto.getEmailAddress()));
        User u = Optional.of(userRepository.findByEmailAddress(user.getEmailAddress()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!u.getEmailAddress().equals(dto.getEmailAddress())
                && userRepository.existsByEmailAddress(dto.getEmailAddress())) {
            throw new AppException("Email address already in use", ErrorType.VALIDATION_ERROR);
        }

        u.setEmailAddress(dto.getEmailAddress());

        if (u instanceof Customer) {
            Customer c = (Customer) u;
            c.setFullName(dto.getFullName());
            customerRepository.save(c);
            u = c;
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

        userRepository.save(u);

        UserDto userDto = modelMapper.map(u, UserDto.class);
        userDto.setFullName(dto.getFullName());
        LoggingUtils.logBusinessEvent(log, "USER_PROFILE_UPDATED", "userId", u.getId(), "email", LoggingUtils.maskSensitiveData(u.getEmailAddress()));
        return userDto;
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        LoggingUtils.logMethodEntry(log, "requestPasswordReset", "email", LoggingUtils.maskSensitiveData(email));
        User user = Optional.of(userRepository.findByEmailAddress(email))
                .orElseThrow(() ->
                        new ResourceNotFoundException("No user found with email: " + email));

        Confirmation confirmation = new Confirmation(user);
        confirmationRepository.save(confirmation);

        eventPublisher.publishEvent(new UserEvent(user, EventType.RESET_PASSWORD, Map.of("key", confirmation.getKey())));
        LoggingUtils.logBusinessEvent(log, "PASSWORD_RESET_REQUESTED", "userId", user.getId(), "email", LoggingUtils.maskSensitiveData(email));
    }

    @Override
    @Transactional
    public void resetPassword(String key, PasswordDto passwordDto) {
        LoggingUtils.logMethodEntry(log, "resetPassword", "key", LoggingUtils.maskSensitiveData(key));
        if (!passwordDto.getPassword().equals(passwordDto.getConfirmPassword())) {
            throw new AppException("Passwords are not matching", ErrorType.VALIDATION_ERROR);
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
        LoggingUtils.logBusinessEvent(log, "PASSWORD_RESET_COMPLETED", "userId", user.getId(), "email", LoggingUtils.maskSensitiveData(user.getEmailAddress()));
    }

    @Override
    public User getAuthenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailAddress = authentication.getName();
        return userRepository.findByEmailAddress(emailAddress);
    }

    private Confirmation getUserConfirmation(String key) {
        return Optional.of(confirmationRepository.findByKey(key))
                .orElseThrow(() -> new AppException("Confirmation key was not found in database", ErrorType.RESOURCE_NOT_FOUND));
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