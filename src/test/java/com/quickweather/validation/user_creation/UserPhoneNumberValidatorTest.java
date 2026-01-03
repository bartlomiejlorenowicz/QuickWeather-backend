package com.quickweather.validation.user_creation;

import com.quickweather.dto.user.RegisterUserRequest;
import com.quickweather.dto.user.UserDto;
import com.quickweather.exceptions.UserValidationException;
import com.quickweather.validation.user.user_creation.UserPhoneNumberValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserPhoneNumberValidatorTest {

    @InjectMocks
    private UserPhoneNumberValidator userPhoneNumberValidator;

    @Test
    void givenPhoneNumber_whenPhoneNumberIsOk_thenDoesNotThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .phoneNumber("2343234511")
                .build();

        assertDoesNotThrow(() -> userPhoneNumberValidator.validate(request));
    }

    @Test
    void givenPhoneNumber_whenPhoneNumberIsNull_thenThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .phoneNumber(null)
                .build();

        UserValidationException exception = assertThrows(UserValidationException.class, () -> userPhoneNumberValidator.validate(request));
        assertEquals("phone number is null", exception.getMessage());
    }

    @Test
    void givenPhoneNumber_whenPhoneNumberTooShort_thenThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .phoneNumber("123")
                .build();

        UserValidationException exception = assertThrows(UserValidationException.class, () -> userPhoneNumberValidator.validate(request));
        assertEquals("phone number must have at least 10 digits", exception.getMessage());
    }

    @Test
    void givenPhoneNumber_whenPhoneNumberTooLong_thenThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .phoneNumber("1".repeat(16))
                .build();

        UserValidationException exception = assertThrows(UserValidationException.class, () -> userPhoneNumberValidator.validate(request));
        assertEquals("phone number must have maximum 15 digits", exception.getMessage());
    }
}