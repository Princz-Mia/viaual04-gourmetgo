package com.princz_mia.viaual04_gourmetgo_backend.config.security.jwt;

import com.princz_mia.viaual04_gourmetgo_backend.config.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JWTTokenProvider  {

    @Value("${auth.token.jwtSecret}")
    private String jwtSecret;

    @Value("${auth.token.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    @Value("${auth.token.refreshExpirationInMs}")
    private int refreshExpirationInMs;

    private final RedisTemplate<String, String> redisTemplate;

    public JWTTokenProvider(@Lazy RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public TokenPair generateTokenPair(Authentication authentication) {
        CustomUserDetails customUserPrincipal = (CustomUserDetails) authentication.getPrincipal();
        String tokenId = UUID.randomUUID().toString();

        Date now = new Date();
        Date accessExpiry = new Date(now.getTime() + jwtExpirationInMs);
        Date refreshExpiry = new Date(now.getTime() + refreshExpirationInMs);

        String accessToken = Jwts.builder()
                .setSubject(customUserPrincipal.getUsername())
                .claim("id", customUserPrincipal.getUser().getId())
                .claim("email", customUserPrincipal.getUser().getEmailAddress())
                .claim("role", customUserPrincipal.getAuthorities())
                .claim("tokenId", tokenId)
                .setIssuedAt(now)
                .setExpiration(accessExpiry)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(customUserPrincipal.getUsername())
                .claim("tokenId", tokenId)
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(refreshExpiry)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();

        return new TokenPair(accessToken, refreshToken);
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
    
    public String getTokenId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("tokenId", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String authToken) {
        try{
            Claims claims = Jwts.parser()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(authToken)
                    .getBody();
            
            String tokenId = claims.get("tokenId", String.class);
            if (tokenId != null && isTokenBlacklisted(tokenId)) {
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException(e.getMessage());
        }
    }

    public void blacklistToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String tokenId = claims.get("tokenId", String.class);
            if (tokenId != null) {
                long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    try {
                        redisTemplate.opsForValue().set("blacklist:" + tokenId, "true", ttl, TimeUnit.MILLISECONDS);
                    } catch (Exception redisException) {
                        // Redis not available, token blacklisting will be skipped
                        System.out.println("Redis not available for token blacklisting: " + redisException.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // Token already invalid, no need to blacklist
        }
    }

    private boolean isTokenBlacklisted(String tokenId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + tokenId));
        } catch (Exception e) {
            // Redis not available, skip blacklist check
            return false;
        }
    }

    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;

        public TokenPair(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }
}
