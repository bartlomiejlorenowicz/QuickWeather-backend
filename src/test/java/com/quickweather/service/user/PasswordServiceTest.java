package com.quickweather.service.user;

import com.quickweather.domain.user.User;
import com.quickweather.dto.user.user_auth.ChangePasswordRequest;
import com.quickweather.exceptions.UserChangePasswordValidationException;
import com.quickweather.exceptions.UserErrorType;
import com.quickweather.exceptions.UserNotFoundException;
import com.quickweather.repository.UserRepository;
import com.quickweather.validation.user.user_change_password.ChangePasswordValidator;
import com.quickweather.validation.user.user_change_password.ChangePasswordValidatorChain;
import com.quickweather.validation.user.user_change_password.ConfirmPasswordValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private UserSearchService userSearchService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChangePasswordValidatorChain changePasswordValidatorChain;

    @InjectMocks
    private PasswordService passwordService;

    @Test
    void shouldThrowsUserNotFoundExceptionWhenUserNotFound() {
        String email = "bartek123@wp.pl";
        ChangePasswordRequest request = new ChangePasswordRequest();

        when(userSearchService.findByEmail(email)).thenReturn(null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> passwordService.changePassword(email, request));

        assertTrue(exception.getMessage().contains("User not found."));
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        String email = "bartek123@wp.pl";
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("newPass");
        request.setConfirmPassword("newPass");

        User user = new User();
        user.setEmail(email);
        user.setPassword("encodedOldPass");

        when(userSearchService.findByEmail(email)).thenReturn(user);

        ChangePasswordValidator validator = mock(ChangePasswordValidator.class);
        when(changePasswordValidatorChain.buildChain()).thenReturn(validator);

        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
        passwordService.changePassword(email, request);

        verify(validator).validate(user, request, passwordEncoder);
        assertEquals("encodedNewPass", user.getPassword());
        verify(userRepository).save(user);

        // Sprawdzenie, czy SecurityContext został wyczyszczony
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldThrowsUserChangePasswordValidationExceptionWhenNewPasswordAndConfirmPasswordDoNotMatch() {
        String email = "bartek123@wp.pl";
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("newPass1");
        request.setConfirmPassword("newPass2");

        User user = new User();
        user.setEmail(email);

        when(userSearchService.findByEmail(email)).thenReturn(user);

        ConfirmPasswordValidator confirmValidator = mock(ConfirmPasswordValidator.class);
        doThrow(new UserChangePasswordValidationException(
                UserErrorType.INVALID_CURRENT_PASSWORD, "Passwords do not match."))
                .when(confirmValidator).validate(any(User.class), any(ChangePasswordRequest.class), any(PasswordEncoder.class));

        when(changePasswordValidatorChain.buildChain()).thenReturn(confirmValidator);

        UserChangePasswordValidationException exception = assertThrows(UserChangePasswordValidationException.class, () ->
                passwordService.changePassword(email, request));

        assertTrue(exception.getMessage().contains("Passwords do not match."));
    }

    @Test
    void shouldThrowExceptionWhenCurrentPasswordIsIncorrect() {
        String email = "bartek123@wp.pl";
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPass");
        request.setConfirmPassword("newPass");

        User user = new User();
        user.setEmail(email);
        user.setPassword("encodedPassword");

        when(userSearchService.findByEmail(email)).thenReturn(user);

        ChangePasswordValidator validator = mock(ChangePasswordValidator.class);
        doThrow(new UserChangePasswordValidationException(UserErrorType.INVALID_CURRENT_PASSWORD, "Current password is incorrect."))
                .when(validator).validate(any(User.class), any(ChangePasswordRequest.class), any(PasswordEncoder.class));

        when(changePasswordValidatorChain.buildChain()).thenReturn(validator);

        UserChangePasswordValidationException exception = assertThrows(
                UserChangePasswordValidationException.class,
                () -> passwordService.changePassword(email, request)
        );
        assertTrue(exception.getMessage().contains("Current password is incorrect."));
    }

}