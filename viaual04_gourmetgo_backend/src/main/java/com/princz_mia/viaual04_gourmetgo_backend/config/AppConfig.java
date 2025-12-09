package com.princz_mia.viaual04_gourmetgo_backend.config;

import com.princz_mia.viaual04_gourmetgo_backend.config.logging.RequestTrackingFilter;
import com.princz_mia.viaual04_gourmetgo_backend.config.security.jwt.JWTAuthEntryPoint;
import com.princz_mia.viaual04_gourmetgo_backend.config.security.CustomUserDetailsService;
import com.princz_mia.viaual04_gourmetgo_backend.config.security.jwt.JWTAuthenticationFilter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class AppConfig {

    private final CustomUserDetailsService customerDetailsService;
    private final JWTAuthEntryPoint jwtAuthEntryPoint;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final RequestTrackingFilter requestTrackingFilter;
    
    public AppConfig(CustomUserDetailsService customerDetailsService,
                    JWTAuthEntryPoint jwtAuthEntryPoint,
                    @Lazy JWTAuthenticationFilter jwtAuthenticationFilter,
                    RequestTrackingFilter requestTrackingFilter) {
        this.customerDetailsService = customerDetailsService;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.requestTrackingFilter = requestTrackingFilter;
    }

    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/login", 
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/auth/csrf",
            "/api/v1/customers/register",
            "/api/v1/restaurants/register",
            "/api/v1/restaurants/verify/**",
            // Public read-only endpoints
            "/api/v1/restaurants",
            "/api/v1/restaurants/*",
            "/api/v1/products",
            "/api/v1/products/*/",
            "/api/v1/products/by-restaurant/**",
            "/api/v1/products/by-name/**",
            "/api/v1/products/by-category/**",
            "/api/v1/products/by-restaurant-and-name",
            "/api/v1/products/by-restaurant-and-category",
            "/api/v1/promotions/happy-hour/active",
            "/api/v1/promotions/category-bonuses/active",
            "/api/v1/reviews/by-restaurant/**",
            "/api/v1/images/**"
    };

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean(name = "taskExecutor")
    public org.springframework.core.task.TaskExecutor taskExecutor() {
        org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor = new org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customerDetailsService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        // Customer-only endpoints
                        .requestMatchers("/api/v1/carts/**", "/api/v1/cartItems/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/v1/rewards/balance/**", "/api/v1/rewards/history/**").hasRole("CUSTOMER")
                        // Admin reward endpoints
                        .requestMatchers("/api/v1/rewards/compensation", "/api/v1/rewards/promotion/**").hasRole("ADMIN")
                        // Admin-only endpoints
                        .requestMatchers("/api/v1/restaurants/pending", "/api/v1/restaurants/*/approve", "/api/v1/restaurants/*/reject").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users", "/api/v1/users/*/lock", "/api/v1/users/*/delete").hasRole("ADMIN")
                        .requestMatchers("/api/v1/promotions/happy-hour", "/api/v1/promotions/category-bonus").hasRole("ADMIN")
                        .requestMatchers("/api/v1/statistics/admin/**", "/api/v1/statistics/online-users").hasRole("ADMIN")
                        .requestMatchers("/api/v1/customers/email/**").hasRole("ADMIN")
                        // Restaurant-only endpoints
                        .requestMatchers("/api/v1/statistics/restaurant/**").hasRole("RESTAURANT")
                        // Authenticated endpoints (any role)
                        .requestMatchers("/api/v1/users/profile").authenticated()
                        .requestMatchers("/api/v1/orders/**", "/api/v1/chat/**", "/api/v1/coupons/**").authenticated()
                        .requestMatchers("/api/v1/reviews/add", "/api/v1/orders/has-ordered/**").authenticated()
                        .requestMatchers("/ws-chat/**", "/ws-statistics/**").permitAll()
                        .anyRequest().permitAll()
                )
                .authenticationProvider(daoAuthenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(requestTrackingFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    



    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                        .addMapping("/api/v1/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("X-CSRF-TOKEN")
                        .allowCredentials(true)
                        .maxAge(3600);
                
                registry
                        .addMapping("/ws-chat/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("X-CSRF-TOKEN")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }

}
