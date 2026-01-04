package com.example.scheduleservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    
    // Internal endpoint - no authentication required
    @GetMapping("/api/users/{userId}")
    UserInfoResponse getUserById(@PathVariable("userId") Long userId);
}
