package com.example.chatservice.middleware;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPrincipal implements UserDetails {

    private Long userId;
    private String email;
    private String fullName;
    private String avatar;

    /* ================= UserDetails ================= */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Chat-service thường không cần role
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return null; // JWT không dùng password
    }

    @Override
    public String getUsername() {
        return email; // Spring dùng username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
