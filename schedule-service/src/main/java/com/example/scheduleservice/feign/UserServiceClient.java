package com.example.scheduleservice.feign;

import com.example.scheduleservice.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/users")
public interface UserServiceClient {
    
    @GetMapping("/{userId}")
    ApiResponse<UserInfoResponse> getUserById(@PathVariable Long userId);
}
