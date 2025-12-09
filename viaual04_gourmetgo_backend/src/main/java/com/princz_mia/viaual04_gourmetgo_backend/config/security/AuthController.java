package com.princz_mia.viaual04_gourmetgo_backend.config.security;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IStatisticsService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.config.security.jwt.JWTTokenProvider;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customerDetailsService;
    private final SessionManagementService sessionManagementService;
    private final IStatisticsService statisticsService;
    private final Map<String, Bucket> rateLimitBuckets = new ConcurrentHashMap<>();
    
    @Value("${auth.token.jwtExpirationInMs}")
    private int accessTokenExpiry;
    
    @Value("${auth.token.refreshExpirationInMs}")
    private int refreshTokenExpiry;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request, 
                                           HttpServletRequest httpRequest, 
                                           HttpServletResponse response) {
        
        if (!isAllowed(getClientIP(httpRequest))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiResponse("Too many login attempts. Please try again later.", null));
        }
        LoggingUtils.logMethodEntry(log, "login", "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
        long startTime = System.currentTimeMillis();
        
        try {
            LoggingUtils.logSecurityEvent(log, "LOGIN_ATTEMPT", "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
            
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            request.getEmailAddress(), request.getPassword()
                    ));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            String userEmail = customUserDetails.getUsername();
            
            // Check concurrent session limit
            if (!sessionManagementService.canCreateNewSession(userEmail)) {
                LoggingUtils.logSecurityEvent(log, "LOGIN_FAILED_MAX_SESSIONS", "email", LoggingUtils.maskSensitiveData(userEmail));
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new ApiResponse("Maximum concurrent sessions reached. Please logout from other devices.", null));
            }
            
            JWTTokenProvider.TokenPair tokenPair = jwtTokenProvider.generateTokenPair(authentication);
            String sessionId = jwtTokenProvider.getTokenId(tokenPair.getAccessToken());
            
            // Register session
            sessionManagementService.addSession(userEmail, sessionId);
            
            // Track visit for statistics and update login attempts
            statisticsService.trackVisit(sessionId, userEmail, getClientIP(httpRequest), httpRequest.getHeader("User-Agent"));
            
            // Update user login attempts and last login
            customerDetailsService.updateUserLoginSuccess(userEmail);
            
            // Set httpOnly cookies
            setTokenCookies(response, tokenPair);
            
            LoginResponse loginResponse = new LoginResponse(customUserDetails.getUser().getId(), null);

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
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("No refresh token found", null));
        }
        
        try {
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse("Invalid refresh token", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Token validation failed: " + e.getMessage(), null));
        }
        
        try {
            String userEmail = jwtTokenProvider.getUserEmailFromJWT(refreshToken);
            CustomUserDetails userDetails = (CustomUserDetails) customerDetailsService.loadUserByUsername(userEmail);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            JWTTokenProvider.TokenPair newTokenPair = jwtTokenProvider.generateTokenPair(auth);
            setTokenCookies(response, newTokenPair);
            
            return ResponseEntity.ok(new ApiResponse("Token refreshed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Token refresh failed", null));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName()) || "refreshToken".equals(cookie.getName())) {
                    if (cookie.getValue() != null) {
                        jwtTokenProvider.blacklistToken(cookie.getValue());
                        // Remove session
                        try {
                            String userEmail = jwtTokenProvider.getUserEmailFromJWT(cookie.getValue());
                            String sessionId = jwtTokenProvider.getTokenId(cookie.getValue());
                            sessionManagementService.removeSession(userEmail, sessionId);
                            statisticsService.removeSession(sessionId);
                        } catch (Exception e) {
                            // Ignore errors during logout
                        }
                    }
                    
                    Cookie expiredCookie = new Cookie(cookie.getName(), null);
                    expiredCookie.setMaxAge(0);
                    expiredCookie.setPath("/");
                    expiredCookie.setHttpOnly(true);
                    expiredCookie.setSecure(false);
                    response.addCookie(expiredCookie);
                }
            }
        }
        
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new ApiResponse("Logged out successfully", null));
    }
    
    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> getCsrfToken(CsrfToken csrfToken) {
        return ResponseEntity.ok(Map.of("token", csrfToken.getToken()));
    }
    
    private void setTokenCookies(HttpServletResponse response, JWTTokenProvider.TokenPair tokenPair) {
        Cookie accessCookie = new Cookie("accessToken", tokenPair.getAccessToken());
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(accessTokenExpiry / 1000);
        response.addCookie(accessCookie);
        
        Cookie refreshCookie = new Cookie("refreshToken", tokenPair.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(refreshTokenExpiry / 1000);
        response.addCookie(refreshCookie);
    }
    
    private boolean isAllowed(String clientIP) {
        Bucket bucket = rateLimitBuckets.computeIfAbsent(clientIP, k -> 
            Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                .build()
        );
        return bucket.tryConsume(1);
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}