package com.quickweather.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickweather.dto.user.UserDto;
import com.quickweather.domain.user.Role;
import com.quickweather.domain.user.RoleType;
import com.quickweather.domain.user.User;
import com.quickweather.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class UserMapper {

    private final ObjectMapper objectMapper;

    private final RoleRepository roleRepository;

    public UserMapper(ObjectMapper objectMapper, RoleRepository roleRepository) {
        this.objectMapper = objectMapper;
        this.roleRepository = roleRepository;
    }

    public User toEntity(UserDto userDto) {
        log.info("Mapping UserDto to User entity for email: {}", userDto.getEmail());

        Role userRole = roleRepository.findByRoleType(RoleType.USER)
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));

        return User.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .phoneNumber(userDto.getPhoneNumber())
                .isEnabled(true)
                .isLocked(userDto.isLocked())
                .roles(Set.of(userRole))
                .uuid(UUID.randomUUID())
                .build();
    }

    public UserDto toDto(User user) {
        log.info("Mapping User entity to UserDto for email: {}", user.getEmail());
        return UserDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .isEnabled(user.isEnabled())
                .isLocked(user.isLocked())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
