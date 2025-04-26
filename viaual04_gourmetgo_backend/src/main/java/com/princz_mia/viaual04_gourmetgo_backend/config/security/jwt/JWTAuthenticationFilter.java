package com.princz_mia.viaual04_gourmetgo_backend.config.security.jwt;

import com.princz_mia.viaual04_gourmetgo_backend.config.security.CustomUserDetails;
import com.princz_mia.viaual04_gourmetgo_backend.config.security.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private JWTTokenProvider tokenProvider;
    private CustomUserDetailsService customerDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = getJWTFromRequest(request);
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String userEmail = tokenProvider.getUserEmailFromJWT(jwt);
                CustomUserDetails customerPrincipal = (CustomUserDetails) customerDetailsService.loadUserByUsername(userEmail);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(customerPrincipal, null, customerPrincipal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch(JwtException e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage() + " Invalid or expired token, you may login and try again.");
            return;
        } catch(Exception e){
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
