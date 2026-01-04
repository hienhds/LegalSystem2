package com.example.scheduleservice.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtil {
    
    private final JwtUtil jwtUtil;
    
    /**
     * Get current user ID from request Authorization header
     */
    public Long getCurrentUserId(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            try {
                return jwtUtil.extractUserId(token);
            } catch (Exception e) {
                log.error("Failed to extract userId from token", e);
                return null;
            }
        }
        return null;
    }
    
    /**
     * Extract JWT token from Authorization header
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
