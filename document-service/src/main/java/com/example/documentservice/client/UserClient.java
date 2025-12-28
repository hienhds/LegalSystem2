package com.example.documentservice.client;

import com.example.documentservice.dto.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(name = "user-service", path = "api/users/internal/users")
@FeignClient(name = "user-service")
public interface UserClient {
//    @PostMapping("/batch")
//    List<UserSummary> getUsers(
//            @RequestBody Set<Long> ids
//    );

    @GetMapping("/api/users/internal/{id}")
    UserSummary getUserById(@PathVariable("id") Long id);
}
