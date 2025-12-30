package com.quickweather.validation.user.user_change_password;

import com.quickweather.domain.user.User;
import com.quickweather.dto.user.user_auth.ChangePasswordRequest;
import com.quickweather.exceptions.UserChangePasswordValidationException;
import com.quickweather.exceptions.UserErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CurrentPasswordValidatorTest {

    @Mock
    private PasswordEncoder encoder;

    private final CurrentPasswordValidator currentPasswordValidator = new CurrentPasswordValidator();

    @Test
    void shouldNotThrowExceptionWhenPasswordMatches() {

        User user = new User();
        user.setPassword("Pass123!");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("Pass123!");
        request.setNewPassword("newPass123!");

        Mockito.when(encoder.matches(request.getCurrentPassword(), user.getPassword())).thenReturn(true);

        assertDoesNotThrow(() -> currentPasswordValidator.validate(user, request, encoder));
        Mockito.verify(encoder).matches(request.getCurrentPassword(), user.getPassword());
    }

    @Test
    void shouldThrowExceptionWhenPasswordNotMatches() {

        User user = new User();
        user.setPassword("Pass123!");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("Pass!");

        Mockito.when(encoder.matches(request.getCurrentPassword(), user.getPassword())).thenReturn(false);

        assertThrows(UserChangePasswordValidationException.class, () -> currentPasswordValidator.validate(user, request, encoder));
        Mockito.verify(encoder).matches(request.getCurrentPassword(), user.getPassword());
    }

    @Test
    void shouldThrowExceptionWhenRequestPasswordNull() {

        User user = new User();
        user.setPassword("Pass123!");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(null);

        Mockito.when(encoder.matches(request.getCurrentPassword(), user.getPassword())).thenReturn(false);

        UserChangePasswordValidationException exception =
                assertThrows(UserChangePasswordValidationException.class, () -> currentPasswordValidator.validate(user, request, encoder));
        assertEquals("Current password is incorrect.", exception.getMessage());
        assertEquals(UserErrorType.INVALID_CURRENT_PASSWORD, exception.getUserErrorType());
        Mockito.verify(encoder).matches(request.getCurrentPassword(), user.getPassword());
    }
    @Test
    void shouldThrowExceptionWhenRequestPasswordNullAndUserPasswordNull() {

        User user = new User();
        user.setPassword(null);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(null);

        Mockito.when(encoder.matches(request.getCurrentPassword(), user.getPassword())).thenReturn(false);

        UserChangePasswordValidationException exception =
                assertThrows(UserChangePasswordValidationException.class, () -> currentPasswordValidator.validate(user, request, encoder));
        assertEquals("Current password is incorrect.", exception.getMessage());
        assertEquals(UserErrorType.INVALID_CURRENT_PASSWORD, exception.getUserErrorType());
    }


}