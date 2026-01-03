package com.quickweather.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickweather.dto.user.RegisterUserRequest;
import com.quickweather.dto.user.UserDto;
import com.quickweather.domain.user.Role;
import com.quickweather.domain.user.RoleType;
import com.quickweather.domain.user.User;
import com.quickweather.dto.user.UserResponse;
import com.quickweather.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final RoleRepository roleRepository;

    public User toEntity(RegisterUserRequest request) {
        log.info("Mapping RegisterUserRequest to User entity");

        Role userRole = roleRepository.findByRoleType(RoleType.USER)
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));

        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phoneNumber(request.getPhoneNumber())
                .isEnabled(true)
                .isLocked(false)
                .roles(Set.of(userRole))
                .uuid(UUID.randomUUID())
                .build();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .uuid(user.getUuid())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(
                        user.getRoles()
                                .stream()
                                .map(r -> r.getRoleType().name())
                                .collect(Collectors.toSet())
                )
                .enabled(user.isEnabled())
                .locked(user.isLocked())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
