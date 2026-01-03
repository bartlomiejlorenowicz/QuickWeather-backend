package com.quickweather.validation.user_creation;

import com.quickweather.dto.user.RegisterUserRequest;
import com.quickweather.dto.user.UserDto;
import com.quickweather.exceptions.UserValidationException;
import com.quickweather.validation.user.user_creation.UserLastNameValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class UserLastNameValidatorTest {

    @InjectMocks
    private UserLastNameValidator userLastNameValidator;

    @Test
    void validLastName_whenLastNameIsNull_thenThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .lastName(null)
                .build();

        Assertions.assertThrows(UserValidationException.class, () -> userLastNameValidator.validate(request));
    }

    @Test
    void validLastName_whenLastNameIsTooShort_thenThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .lastName("l")
                .build();

        Assertions.assertThrows(UserValidationException.class, () -> userLastNameValidator.validate(request));
    }

    @Test
    void validLastName_whenLastNameIsTooLong_thenThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .lastName("l".repeat(31))
                .build();

        Assertions.assertThrows(UserValidationException.class, () -> userLastNameValidator.validate(request));
    }

    @Test
    void validLastName_whenLastNameIsOk_thenDoesNotThrowException() {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .lastName("lastname")
                .build();

        assertDoesNotThrow(() -> userLastNameValidator.validate(request));
    }
}