package com.princz_mia.viaual04_gourmetgo_backend.config.security;

import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.config.security.jwt.JWTTokenProvider;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        LoggingUtils.logMethodEntry(log, "login", "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
        long startTime = System.currentTimeMillis();
        
        try {
            LoggingUtils.logSecurityEvent(log, "LOGIN_ATTEMPT", "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
            
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            request.getEmailAddress(), request.getPassword()
                    ));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtTokenProvider.generateToken(authentication);
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            LoginResponse loginResponse = new LoginResponse(customUserDetails.getUser().getId(), token);

            LoggingUtils.logSecurityEvent(log, "LOGIN_SUCCESS", "userId", customUserDetails.getUser().getId(), "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
            LoggingUtils.logBusinessEvent(log, "USER_AUTHENTICATED", "userId", customUserDetails.getUser().getId());
            LoggingUtils.logPerformance(log, "login", System.currentTimeMillis() - startTime);

            return ResponseEntity.ok(new ApiResponse("Login Successful", loginResponse));
        } catch (LockedException ex) {
            LoggingUtils.logSecurityEvent(log, "LOGIN_FAILED_ACCOUNT_LOCKED", "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()), "reason", "account_locked");
            return ResponseEntity
                    .status(HttpStatus.LOCKED)
                    .body(new ApiResponse("Your account has been locked.", null));
        } catch (DisabledException ex) {
            LoggingUtils.logSecurityEvent(log, "LOGIN_FAILED_ACCOUNT_DISABLED", "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()), "reason", "account_disabled");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Your account is not verified.", null));
        } catch (BadCredentialsException ex) {
            LoggingUtils.logSecurityEvent(log, "LOGIN_FAILED_BAD_CREDENTIALS", "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()), "reason", "bad_credentials");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Invalid email or password.", null));
        } catch (AuthenticationException ex) {
            LoggingUtils.logSecurityEvent(log, "LOGIN_FAILED_AUTH_ERROR", "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()), "reason", "authentication_error");
            LoggingUtils.logError(log, "Authentication failed", ex, "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Authentication failed.", null));
        }
    }
}