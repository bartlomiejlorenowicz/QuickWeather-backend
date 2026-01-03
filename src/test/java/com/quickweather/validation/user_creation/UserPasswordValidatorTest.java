package com.quickweather.validation.user_creation;

import com.quickweather.dto.user.RegisterUserRequest;
import com.quickweather.dto.user.UserDto;
import com.quickweather.exceptions.UserValidationException;
import com.quickweather.validation.user.user_creation.UserPasswordValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserPasswordValidatorTest {

    @InjectMocks
    private UserPasswordValidator userPasswordValidator;

    @Test
    void givenPassword_whenPasswordOk_thenDoesNotThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .password("pass123!")
                .build();

        assertDoesNotThrow(() -> userPasswordValidator.validate(request));
    }

    @Test
    void givenPassword_whenPasswordIsNull_thenThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .password(null)
                .build();

        assertThrows(UserValidationException.class, () -> userPasswordValidator.validate(request));
    }

    @Test
    void givenPassword_whenPasswordTooShort_thenThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .password("abc")
                .build();

        UserValidationException exception = assertThrows(UserValidationException.class, () -> userPasswordValidator.validate(request));
        assertEquals("password must be minimum 8 characters long", exception.getMessage());
    }

    @Test
    void givenPassword_whenPasswordWithoutSpecialCharacter_thenThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .password("computer123")
                .build();

        UserValidationException exception = assertThrows(UserValidationException.class, () -> userPasswordValidator.validate(request));
        assertEquals("password does not contain a special character", exception.getMessage());
    }

}