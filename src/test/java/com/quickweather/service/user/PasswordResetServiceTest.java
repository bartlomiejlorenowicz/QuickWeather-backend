package com.quickweather.service.user;

import com.quickweather.domain.user.User;
import com.quickweather.dto.user.user_auth.SetNewPasswordRequest;
import com.quickweather.repository.UserRepository;
import com.quickweather.security.JwtUtil;
import com.quickweather.service.email.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenUserNotFoundOnForgotPassword() {
        String email = "test@wp.pl";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> passwordResetService.forgotPassword(email)
        );
    }

    @Test
    void shouldSendForgotPasswordEmailSuccessfully() {
        String email = "test@wp.pl";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.generateResetToken(user)).thenReturn("token123");

        passwordResetService.forgotPassword(email);

        verify(emailService).sendForgotPasswordEmail(
                eq(email),
                contains("token123")
        );
    }

    @Test
    void shouldThrowUnauthorizedWhenTokenIsInvalid() {
        SetNewPasswordRequest request = new SetNewPasswordRequest();
        request.setToken("invalid");

        when(jwtUtil.validateResetToken("invalid")).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> passwordResetService.resetPasswordUsingToken(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void shouldThrowNotFoundWhenUserNotFound() {
        SetNewPasswordRequest request = new SetNewPasswordRequest();
        request.setToken("valid");

        when(jwtUtil.validateResetToken("valid")).thenReturn(true);
        when(jwtUtil.extractUsernameFromResetToken("valid")).thenReturn("missing@wp.pl");
        when(userRepository.findByEmail("missing@wp.pl")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> passwordResetService.resetPasswordUsingToken(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestWhenPasswordsDoNotMatch() {
        SetNewPasswordRequest request = new SetNewPasswordRequest();
        request.setToken("valid");
        request.setNewPassword("newPass");
        request.setConfirmPassword("otherPass");

        User user = new User();
        user.setEmail("test@wp.pl");

        when(jwtUtil.validateResetToken("valid")).thenReturn(true);
        when(jwtUtil.extractUsernameFromResetToken("valid")).thenReturn("test@wp.pl");
        when(userRepository.findByEmail("test@wp.pl")).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> passwordResetService.resetPasswordUsingToken(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void shouldResetPasswordSuccessfully() {
        SetNewPasswordRequest request = new SetNewPasswordRequest();
        request.setToken("valid");
        request.setNewPassword("newPass");
        request.setConfirmPassword("newPass");

        User user = new User();
        user.setEmail("test@wp.pl");

        when(jwtUtil.validateResetToken("valid")).thenReturn(true);
        when(jwtUtil.extractUsernameFromResetToken("valid")).thenReturn("test@wp.pl");
        when(userRepository.findByEmail("test@wp.pl")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedPass");

        passwordResetService.resetPasswordUsingToken(request);

        assertEquals("encodedPass", user.getPassword());
        verify(userRepository).save(user);
    }
}
