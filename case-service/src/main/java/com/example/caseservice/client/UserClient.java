package com.example.caseservice.client;

import com.example.caseservice.dto.ApiResponse;
import com.example.caseservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service") // Tên service đã đăng ký trên Eureka
public interface UserClient {

    @GetMapping("/api/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") Long id);
}