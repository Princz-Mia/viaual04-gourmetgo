package com.princz_mia.viaual04_gourmetgo_backend.config.security;

import com.princz_mia.viaual04_gourmetgo_backend.admin.IAdminService;
import com.princz_mia.viaual04_gourmetgo_backend.config.security.jwt.JWTTokenProvider;
import com.princz_mia.viaual04_gourmetgo_backend.customer.ICustomerService;
import com.princz_mia.viaual04_gourmetgo_backend.response.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant.IRestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider jwtTokenProvider;

    private final ICustomerService customerService;
    private final IRestaurantService restaurantService;
    private final IAdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            request.getEmailAddress(), request.getPassword()
                    ));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtTokenProvider.generateToken(authentication);
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            LoginResponse loginResponse = new LoginResponse(customUserDetails.getUser().getId(), token);

            return ResponseEntity.ok(new ApiResponse("Login Successful", loginResponse));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
        }
    }
}
