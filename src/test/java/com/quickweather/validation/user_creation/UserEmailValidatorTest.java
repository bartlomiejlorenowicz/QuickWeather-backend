package com.quickweather.validation.user_creation;

import com.quickweather.dto.user.RegisterUserRequest;
import com.quickweather.dto.user.UserDto;
import com.quickweather.exceptions.UserValidationException;
import com.quickweather.repository.UserRepository;
import com.quickweather.validation.user.user_creation.UserEmailValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEmailValidatorTest {

    @Mock
    private UserRepository userCreationRepository;

    @InjectMocks
    private UserEmailValidator userEmailValidator;

    @Test
    void givenEmail_WhenEmailOk_thenDoesNotThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("first@wp.pl")
                .build();

        assertDoesNotThrow(() -> userEmailValidator.validate(request));
    }

    @Test
    void givenEmail_WhenEmailNotOk_thenThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("first.pl")
                .build();

        assertThrows(UserValidationException.class, () -> userEmailValidator.validate(request));
    }

    @Test
    void givenEmail_WhenEmailIsNull_thenThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .email(null)
                .build();

        assertThrows(UserValidationException.class, () -> userEmailValidator.validate(request));
    }

    @Test
    void givenEmail_WhenEmailExist_thenThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .email("first@wp.pl")
                .build();
        when(userCreationRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(UserValidationException.class, () -> userEmailValidator.validate(request));
    }
}