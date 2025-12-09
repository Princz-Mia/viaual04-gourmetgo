package com.princz_mia.viaual04_gourmetgo_backend.config.security;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Credential;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.CredentialRepository;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.User;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;

    @Override
    public UserDetails loadUserByUsername(String emailAddress) throws UsernameNotFoundException {
        User user = Optional.ofNullable(userRepository.findByEmailAddress(emailAddress))
                .orElseThrow(() -> new UsernameNotFoundException("User with email address " + emailAddress + " not found"));

        Credential credential = Optional.ofNullable(credentialRepository.findByUser_Id(user.getId()))
                .orElseThrow(() -> new UsernameNotFoundException("Credential with id " + user.getId() + " not found"));

        return new CustomUserDetails(user, credential);
    }
    
    @Transactional
    public void updateUserLoginSuccess(String emailAddress) {
        User user = userRepository.findByEmailAddress(emailAddress);
        if (user != null) {
            user.setLastLogin(LocalDateTime.now());
            user.setLoginAttempts(user.getLoginAttempts() != null ? user.getLoginAttempts() + 1 : 1);
            userRepository.save(user);
        }
    }
}
