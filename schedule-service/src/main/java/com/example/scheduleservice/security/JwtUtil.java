package com.example.scheduleservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Error parsing JWT token: {}", e.getMessage());
            return null;
        }
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) return null;
        
        // User-service uses "uid" claim for userId
        Object userIdObj = claims.get("uid");
        
        if (userIdObj == null) {
            // Fallback to other possible names
            userIdObj = claims.get("userId");
        }
        if (userIdObj == null) {
            userIdObj = claims.get("id");
        }
        
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException e) {
                log.error("Cannot parse userId from string: {}", userIdObj);
            }
        }
        
        log.warn("No userId found in token. Available claims: {}", claims.keySet());
        return null;
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) return null;
        
        // Try to get role from claims
        String role = claims.get("role", String.class);
        if (role == null) {
            role = claims.get("roles", String.class);
        }
        if (role == null) {
            role = claims.get("authorities", String.class);
        }
        
        // User-service doesn't include role in token
        // We need to fetch role from user-service or database
        if (role == null) {
            log.debug("No role found in token. Will need to fetch from database.");
        }
        
        return role;
    }

    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) return null;
        return claims.getSubject();
    }
}
