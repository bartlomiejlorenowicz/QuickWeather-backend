package com.quickweather.validation.user.user_change_password;

import com.quickweather.domain.user.User;
import com.quickweather.dto.user.user_auth.ChangePasswordRequest;
import com.quickweather.exceptions.UserChangePasswordValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NewPasswordDifferentValidatorTest {

    @Mock
    private PasswordEncoder encoder;

    NewPasswordDifferentValidator newPasswordDiffValidator = new NewPasswordDifferentValidator();

    @Test
    void shouldNotThrowExceptionWhenNewPasswordIsDifferent() {
        User user = new User();
        user.setPassword("Pass123");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("Pass123!");
        request.setConfirmPassword("Pass123!");

        Mockito.when(encoder.matches(request.getNewPassword(), user.getPassword())).thenReturn(false);

        assertDoesNotThrow(() -> newPasswordDiffValidator.validate(user, request, encoder));

    }

    @Test
    void shouldThrowExceptionWhenNewPasswordIsSameAsOld() {
        User user = new User();
        user.setPassword("Pass123");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("Pass123");
        request.setConfirmPassword("Pass123");

        Mockito.when(encoder.matches(request.getNewPassword(), user.getPassword())).thenReturn(true);

        UserChangePasswordValidationException exception =
                assertThrows(UserChangePasswordValidationException.class, () -> newPasswordDiffValidator.validate(user, request, encoder));
        assertEquals("New password must be different from the old password.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNewPasswordIsNull() {
        User user = new User();
        user.setPassword("Pass123");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword(null);
        request.setConfirmPassword(null);

        Mockito.when(encoder.matches(request.getNewPassword(), user.getPassword())).thenReturn(true);

        UserChangePasswordValidationException exception =
                assertThrows(UserChangePasswordValidationException.class, () -> newPasswordDiffValidator.validate(user, request, encoder));
        assertEquals("New password must be different from the old password.", exception.getMessage());
    }

}