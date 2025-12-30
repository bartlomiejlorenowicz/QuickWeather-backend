package com.quickweather.service.user;

import com.quickweather.domain.user.User;
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
        UserDto dto = new UserDto();
        dto.setEmail("test@example.com");
        dto.setFirstName("Test");

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());

        when(userMapper.toEntity(dto)).thenReturn(user);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        // when
        userCreationService.createUser(dto);

        // then
        verify(emailService).sendWelcomeEmail(
                "test@example.com",
                "Test"
        );
    }

    @Test
    void shouldNotCreateUserWhenValidationFails() {
        // given
        UserDto dto = new UserDto();
        doThrow(new RuntimeException("Validation error"))
                .when(validator).validate(dto);

        // when / then
        assertThrows(RuntimeException.class,
                () -> userCreationService.createUser(dto));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldEncodePasswordBeforeSavingUser() {
        // given
        UserDto dto = new UserDto();
        dto.setPassword("plain123");

        User user = new User();
        when(userMapper.toEntity(dto)).thenReturn(user);
        when(passwordEncoder.encode("plain123")).thenReturn("ENCODED");

        // when
        userCreationService.createUser(dto);

        // then
        verify(passwordEncoder).encode("plain123");
        assertEquals("ENCODED", user.getPassword());
    }

    @Test
    void shouldAssignDefaultUserRole() {
        // given
        UserDto dto = new UserDto();
        User user = new User();
        when(userMapper.toEntity(dto)).thenReturn(user);

        // when
        userCreationService.createUser(dto);

        // then
        verify(userRoleService).assignDefaultUserRole(anySet());
    }
}
