package com.quickweather.service.user;

import com.quickweather.admin.SecurityEventType;
import com.quickweather.domain.user.User;
import com.quickweather.exceptions.UserNotFoundException;
import com.quickweather.repository.UserRepository;
import com.quickweather.service.admin.SecurityEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDeletionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityEventService securityEventService;

    @InjectMocks
    private UserDeletionService userDeletionService;

    @Test
    void shouldDeleteUserSuccessfully() {
        Long userId = 12L;
        User user = new User();
        user.setId(userId);
        user.setEmail("bartek123@wp.pl");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String ipAddress = "192.168.0.1";
        when(securityEventService.getClientIpAddress()).thenReturn(ipAddress);

        userDeletionService.deleteUser(userId);

        verify(userRepository).delete(user);
        verify(securityEventService).logEvent(user.getEmail(), SecurityEventType.ACCOUNT_DELETED, ipAddress);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        Long userId = 12L;
        User user = new User();
        user.setId(userId);
        user.setEmail("bartek123@wp.pl");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows( UserNotFoundException.class, () ->
                userDeletionService.deleteUser(userId));
        assertTrue(exception.getMessage().contains("User not found"));
    }
}