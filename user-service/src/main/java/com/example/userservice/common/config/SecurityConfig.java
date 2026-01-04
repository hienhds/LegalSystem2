package com.example.userservice.common.config;
import com.example.userservice.common.security.CustomUserDetailsService;
import com.example.userservice.common.security.JwtAuthenticationFilter;
import com.example.userservice.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cấu hình bảo mật chính của hệ thống.
 * - Stateless (JWT)
 * - Cho phép truy cập các endpoint công khai như đăng ký, đăng nhập, verify, reset password
 * - Bảo vệ các API khác yêu cầu xác thực JWT
 */
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtProvider, customUserDetailsService);

        http
                // Tắt CSRF vì ta dùng JWT
                .csrf(csrf -> csrf.disable())

                // Cấu hình session là stateless (vì JWT không cần session)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Cấu hình ủy quyền request
                .authorizeHttpRequests(auth -> auth
                        // ✅ Các endpoint công khai
                        .requestMatchers(
                                "/api/users/internal/**",

                                "/search/**",
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/verify",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ✅ Cho phép truy cập tĩnh (nếu bạn có file ảnh, css,…)
                        .requestMatchers(
                                "/resources/**",
                                "/static/**",
                                "/images/**",
                                "/css/**",
                                "/js/**"
                        ).permitAll()

//                        // USER + LAWYER + ADMIN
//                        .requestMatchers("/api/user/**").hasAnyRole("USER", "LAWYER", "ADMIN")
//
//                        // LAWYER + ADMIN
//                        .requestMatchers("/api/lawyer/**").hasAnyRole("LAWYER", "ADMIN")
//
//                        // ONLY ADMIN
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Các request còn lại cần xác thực
                        .anyRequest().authenticated()
                )

                // Gắn JWT filter vào trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean quản lý xác thực cho AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Bean mã hóa mật khẩu
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
