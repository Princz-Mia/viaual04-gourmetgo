package com.princz_mia.viaual04_gourmetgo_backend.config.security.jwt;

import com.princz_mia.viaual04_gourmetgo_backend.config.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JWTTokenProvider  {

    @Value("${auth.token.jwtSecret}")
    private String jwtSecret;

    @Value("${auth.token.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    public String generateToken(Authentication authentication) {
        CustomUserDetails customUserPrincipal = (CustomUserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(customUserPrincipal.getUsername()) // getUsername provides the emailAddress
                .claim("id", customUserPrincipal.getUser().getId())
                .claim("email", customUserPrincipal.getUser().getEmailAddress())
                .claim("role", customUserPrincipal.getAuthorities())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserEmailFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try{
            Jwts.parser()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException(e.getMessage());
        }
    }
}
