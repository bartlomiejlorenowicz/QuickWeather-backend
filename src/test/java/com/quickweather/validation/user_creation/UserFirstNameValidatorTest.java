package com.quickweather.validation.user_creation;

import com.quickweather.dto.user.RegisterUserRequest;
import com.quickweather.dto.user.UserDto;
import com.quickweather.exceptions.UserValidationException;
import com.quickweather.validation.user.user_creation.UserFirstNameValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserFirstNameValidatorTest {

    @InjectMocks
    private UserFirstNameValidator userFirstNameValidator;

    @Test
    void validateFirstName_whenFirstNameIsNull_thenReturnException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .firstName(null)
                .build();

        assertThrows(UserValidationException.class,
                () -> userFirstNameValidator.validate(request));
    }

    @Test
    void validateFirstName_whenFirstNameIsOk_thenDoesNotThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .firstName("firstname")
                .build();

        assertDoesNotThrow(() -> userFirstNameValidator.validate(request));
    }

    @Test
    void validateFirstName_whenFirstNameIsTooShort_thenThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .firstName("f")
                .build();

        assertThrows(UserValidationException.class, () -> userFirstNameValidator.validate(request));
    }

    @Test
    void validateFirstName_whenFirstNameIsTooLong_thenThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .firstName("f".repeat(31))
                .build();

        assertThrows(UserValidationException.class, () -> userFirstNameValidator.validate(request));
    }
}