package com.quickweather.validation.user.user_change_password;

import com.quickweather.domain.user.User;
import com.quickweather.dto.user.user_auth.ChangePasswordRequest;
import com.quickweather.exceptions.UserChangePasswordValidationException;
import com.quickweather.exceptions.UserErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConfirmPasswordValidatorTest {

    @Mock
    private PasswordEncoder encoder;

    @Test
    void testValidate_WhenPasswordsMatch_NoExceptionThrown() {

        ConfirmPasswordValidator validator = new ConfirmPasswordValidator();
        User user = new User();

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("password123");
        request.setConfirmPassword("password123");

        assertDoesNotThrow(() -> validator.validate(user, request, encoder));
    }

    @Test
    void testValidate_WhenPasswordsDoNotMatch_ExceptionThrown() {

        ConfirmPasswordValidator validator = new ConfirmPasswordValidator();
        User user = new User();

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("password123");
        request.setConfirmPassword("differentPassword");

        UserChangePasswordValidationException exception = assertThrows(
                UserChangePasswordValidationException.class,
                () -> validator.validate(user, request, encoder)
        );
        assertEquals("Passwords do not match.", exception.getMessage());
        assertEquals(UserErrorType.INVALID_CURRENT_PASSWORD, exception.getUserErrorType());
    }
}
