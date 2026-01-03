package com.quickweather.service.user;

import com.quickweather.domain.user.User;
import com.quickweather.dto.user.RegisterUserRequest;
import com.quickweather.dto.user.UserDto;
import com.quickweather.mapper.UserMapper;
import com.quickweather.repository.UserRepository;
import com.quickweather.service.admin.SecurityEventService;
import com.quickweather.service.email.EmailService;
import com.quickweather.validation.user.user_creation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCreationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private UserValidator validator;

    @Mock
    private SecurityEventService securityEventService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserCreationService userCreationService;

    @Test
    void shouldSendWelcomeEmailAfterUserCreation() {
        // given
        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("test@example.com")
                .firstName("Test")
                .password("Password!123")
                .build();

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());

        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        // when
        userCreationService.createUser(request);

        // then
        verify(emailService).sendWelcomeEmail(
                "test@example.com",
                "Test"
        );
    }

    @Test
    void shouldNotCreateUserWhenValidationFails() {
        // given
        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("bad@email")
                .build();

        doThrow(new RuntimeException("Validation error"))
                .when(validator).validate(request);

        // when / then
        assertThrows(RuntimeException.class,
                () -> userCreationService.createUser(request));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldEncodePasswordBeforeSavingUser() {
        // given
        RegisterUserRequest request = RegisterUserRequest.builder()
                .password("plain123!")
                .email("test@test.com")
                .firstName("Test")
                .build();

        User user = new User();

        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("plain123!")).thenReturn("ENCODED");

        // when
        userCreationService.createUser(request);

        // then
        verify(passwordEncoder).encode("plain123!");
        assertEquals("ENCODED", user.getPassword());
    }

    @Test
    void shouldAssignDefaultUserRole() {
        // given
        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("test@test.com")
                .firstName("Test")
                .password("Password!123")
                .build();

        User user = new User();

        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(any())).thenReturn("ENCODED");

        // when
        userCreationService.createUser(request);

        // then
        verify(userRoleService).assignDefaultUserRole(anySet());
    }
}
