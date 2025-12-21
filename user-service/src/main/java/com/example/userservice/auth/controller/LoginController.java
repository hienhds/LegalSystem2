package com.example.userservice.auth.controller;


import com.example.userservice.auth.dto.request.LoginRequest;
import com.example.userservice.auth.dto.request.RefreshRequest;
import com.example.userservice.auth.dto.response.JwtResponse;
import com.example.userservice.auth.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoginController {

    private final LoginService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        JwtResponse jwt = authService.login(request, servletRequest);
        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestBody RefreshRequest request, HttpServletRequest servletRequest) {
        JwtResponse jwt = authService.refresh(request.getRefreshToken(), servletRequest);
        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
