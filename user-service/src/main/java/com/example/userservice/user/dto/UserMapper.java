package com.example.userservice.user.dto;

import com.example.userservice.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "phoneNumber", ignore = true) // Bỏ qua map sđt
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    UserResponse userToRegistrationResponse(User user);
}
