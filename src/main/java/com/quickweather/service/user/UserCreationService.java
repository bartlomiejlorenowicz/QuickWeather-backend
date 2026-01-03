package com.quickweather.service.user;
import com.quickweather.admin.SecurityEventType;
import com.quickweather.dto.user.UserDto;
import com.quickweather.dto.user.UserResponse;
import com.quickweather.mapper.UserMapper;

import com.quickweather.repository.UserRepository;
import com.quickweather.service.admin.SecurityEventService;
import com.quickweather.service.email.EmailService;
import com.quickweather.validation.user.user_creation.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.quickweather.dto.user.RegisterUserRequest;

import java.util.HashSet;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCreationService {

    private final UserValidator validator;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final EmailService emailService;

    private final SecurityEventService securityEventService;

    public UserResponse createUser(RegisterUserRequest registerUserRequest) {
        validator.validate(registerUserRequest);
        log.info("Starting saving user with email: {}", registerUserRequest.getEmail());

        var userEntity = userMapper.toEntity(registerUserRequest);
        userEntity.setPassword(passwordEncoder.encode(registerUserRequest.getPassword()));

        userEntity.setRoles(new HashSet<>());
        userRoleService.assignDefaultUserRole(userEntity.getRoles());

        log.info("Saving User entity to database: {}", userEntity);
        userRepository.save(userEntity);
        log.info("User is saved with roles: {}", userEntity.getRoles());

        emailService.sendWelcomeEmail(
                userEntity.getEmail(),
                userEntity.getFirstName()
        );

        String ipAddress = securityEventService.getClientIpAddress();
        securityEventService.logEvent(registerUserRequest.getEmail(), SecurityEventType.ACCOUNT_CREATED, ipAddress);

        return userMapper.toResponse(userEntity);
    }
}
