package com.example.caseservice.client;

import com.example.caseservice.config.FeignConfig;
import com.example.caseservice.dto.ApiResponse;
import com.example.caseservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
    name = "user-service", 
    url = "${user-service.url:http://user-service}",
    configuration = FeignConfig.class
)
public interface UserClient {
    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users/internal/ids")
    ApiResponse<List<UserResponse>> getUsersByIds(@RequestParam("ids") List<Long> ids);
}