package com.example.caseservice.filter;

import com.example.caseservice.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Long userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);

                if (userId != null) {
                    request.setAttribute("userId", userId);
                    log.debug("Extracted userId from token: {}", userId);
                }

                // If role not in token, set default or fetch from database
                if (role != null) {
                    request.setAttribute("userRole", role);
                    log.debug("Extracted role from token: {}", role);
                } else {
                    // For now, we'll need to determine role by calling user-service
                    // Or we can check in the service layer
                    log.debug("Role not in token, will determine from service");
                }
            } catch (Exception e) {
                log.error("Failed to parse JWT token: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
