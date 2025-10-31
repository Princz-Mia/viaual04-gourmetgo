package com.princz_mia.viaual04_gourmetgo_backend.config.security.jwt;

import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.config.security.CustomUserDetails;
import com.princz_mia.viaual04_gourmetgo_backend.config.security.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTTokenProvider tokenProvider;
    private final CustomUserDetailsService customerDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        
        try {
            String jwt = getJWTFromRequest(request);
            if (StringUtils.hasText(jwt)) {
                if (tokenProvider.validateToken(jwt)) {
                    String userEmail = tokenProvider.getUserEmailFromJWT(jwt);
                    CustomUserDetails customerPrincipal = (CustomUserDetails) customerDetailsService.loadUserByUsername(userEmail);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(customerPrincipal, null, customerPrincipal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    LoggingUtils.logSecurityEvent(log, "JWT_AUTHENTICATION_SUCCESS", "userId", customerPrincipal.getUser().getId(), "email", LoggingUtils.maskSensitiveData(userEmail), "uri", requestURI);
                } else {
                    LoggingUtils.logSecurityEvent(log, "JWT_TOKEN_INVALID", "uri", requestURI);
                }
            }
        } catch(JwtException e){
            LoggingUtils.logSecurityEvent(log, "JWT_TOKEN_ERROR", "error", e.getMessage(), "uri", requestURI);
            LoggingUtils.logError(log, "JWT token validation failed", e, "uri", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage() + " Invalid or expired token, you may login and try again.");
            return;
        } catch(Exception e){
            LoggingUtils.logSecurityEvent(log, "JWT_FILTER_ERROR", "error", e.getMessage(), "uri", requestURI);
            LoggingUtils.logError(log, "JWT filter processing failed", e, "uri", requestURI);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(e.getMessage());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String getJWTFromRequest(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}