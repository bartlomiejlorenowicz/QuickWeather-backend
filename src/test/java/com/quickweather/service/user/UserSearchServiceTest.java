package com.quickweather.service.user;

import com.quickweather.domain.user.User;
import com.quickweather.exceptions.UserNotFoundException;
import com.quickweather.exceptions.UserValidationException;
import com.quickweather.repository.UserRepository;
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
class UserSearchServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSearchService userSearchService;

    @Test
    void shouldFindUserByIdSuccessfully() {
        String email = "bartek123@wp.pl";
        Long userId = 11L;
        User user = new User();
        user.setId(userId);
        user.setEmail(email);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User foundedUser = userSearchService.findById(userId);

        assertNotNull(foundedUser);
        assertEquals(userId, foundedUser.getId());
        assertEquals(email, foundedUser.getEmail());
        verify(userRepository).findById(userId);
    }
    
    @Test
    void shouldThrowsUserValidationExceptionWhenUserNotFound() {
        Long userId = 11L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserValidationException exception = assertThrows(UserValidationException.class, () ->
                userSearchService.findById(userId));

        assertTrue(exception.getMessage().contains("User not found by ID: " + userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void shouldFindUserByEmailSuccessfully() {
        String email = "bartek123@wp.pl";

        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User foundedUser = userSearchService.findByEmail(email);

        assertNotNull(foundedUser);
        assertEquals(email, foundedUser.getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldThrowsUserNotFoundExceptionWhenUserNotFoundByEmail() {
        String email = "bartek123@wp.pl";

        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                userSearchService.findByEmail(email));

        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(userRepository).findByEmail(email);
    }

}