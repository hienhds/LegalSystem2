package com.example.backend.user.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.example.backend.user.entity.User;
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "phoneNumber", ignore = true) // Bỏ qua map sđt
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    UserResponse userToRegistrationResponse(User user);
}
