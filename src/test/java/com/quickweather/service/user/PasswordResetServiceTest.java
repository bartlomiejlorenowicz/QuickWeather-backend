package com.quickweather.service.user;

import com.google.api.services.gmail.Gmail;
import com.quickweather.domain.user.User;
import com.quickweather.dto.user.user_auth.SetNewPasswordRequest;
import com.quickweather.exceptions.EmailSendingException;
import com.quickweather.integration.GmailQuickstart;
import com.quickweather.repository.UserRepository;
import com.quickweather.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserSearchService userSearchService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GmailQuickstart gmailQuickstart;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private static final String VALID_TOKEN = "validToken";
    private static final String INVALID_TOKEN = "invalidToken";
    private static final String EMAIL = "bartek123@wp.pl";
    private final String frontendBaseUrl = "http://localhost:4200";

    private SetNewPasswordRequest buildRequest(String token, String newPassword, String confirmPassword) {
        SetNewPasswordRequest request = new SetNewPasswordRequest();
        request.setToken(token);
        request.setNewPassword(newPassword);
        request.setConfirmPassword(confirmPassword);
        return request;
    }

    private User buildUser(String email) {
        User user = new User();
        user.setEmail(EMAIL);
        return user;
    }

    @Test
    void shouldThrowsResponseStatusExceptionWhenInvalidToken() {
        SetNewPasswordRequest request = new SetNewPasswordRequest();
        request.setToken(INVALID_TOKEN);

        when(jwtUtil.validateResetToken(INVALID_TOKEN)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> passwordResetService.resetPasswordUsingToken(request));
        assertTrue(exception.getMessage().contains("Invalid or expired token."));
        assertTrue(exception.getMessage().contains("401"));
    }

    @Test
    void shouldThrowsResponseStatusExceptionWhenUserNotFound() {
        SetNewPasswordRequest request = buildRequest(VALID_TOKEN, "Bartek123!", "Bartek123!");
        request.setToken(VALID_TOKEN);

        when(jwtUtil.validateResetToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractUsernameFromResetToken(VALID_TOKEN)).thenReturn("user123@wp.pl");
        when(userSearchService.findByEmail("user123@wp.pl")).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> passwordResetService.resetPasswordUsingToken(request));
        assertTrue(exception.getMessage().contains("User not found."));
        assertTrue(exception.getMessage().contains("404"));
    }

    @Test
    void shouldReturnResponseStatusExceptionWhenNewPasswordAndConfirmPasswordDifferent() {
        SetNewPasswordRequest request = buildRequest(VALID_TOKEN, "newPassword!", "newPass");

        User user = buildUser(EMAIL);

        when(jwtUtil.validateResetToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractUsernameFromResetToken(VALID_TOKEN)).thenReturn(user.getEmail());
        when(userSearchService.findByEmail(EMAIL)).thenReturn(user);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> passwordResetService.resetPasswordUsingToken(request));
        assertTrue(exception.getMessage().contains("Passwords do not match."));
        assertTrue(exception.getMessage().contains("400"));

    }

    @Test
    void shouldReturnSuccessfulWhenResetPasswordUsingToken() {
        SetNewPasswordRequest request = buildRequest(VALID_TOKEN, "newPass123!", "newPass123!");

        User user = buildUser(EMAIL);

        when(jwtUtil.validateResetToken(VALID_TOKEN)).thenReturn(true);
        when(jwtUtil.extractUsernameFromResetToken(VALID_TOKEN)).thenReturn(user.getEmail());
        when(userSearchService.findByEmail(EMAIL)).thenReturn(user);
        when(passwordEncoder.encode("newPass123!")).thenReturn("encodedNewPass123!");

        passwordResetService.resetPasswordUsingToken(request);

        verify(userRepository).save(user);
    }

    //============ tests sendPasswordResetEmail(String email, String resetPath) =========

    @Test
    void shouldSendPasswordResetEmailWithSuccess() throws Exception {

        String email = EMAIL;
        String resetPath = "/reset";
        User user = buildUser(EMAIL);
        user.setUuid(UUID.randomUUID());

        String token = "dummyToken";

        when(userSearchService.findByEmail(EMAIL)).thenReturn(user);
        when(jwtUtil.generateResetToken(user)).thenReturn(token);

        ReflectionTestUtils.setField(passwordResetService, "frontendBaseUrl", frontendBaseUrl);

        Gmail gmailService = mock(Gmail.class);
        when(gmailQuickstart.getGmailService()).thenReturn(gmailService);

        passwordResetService.sendPasswordResetEmail(email, resetPath);

        verify(gmailQuickstart).sendEmail(
                eq(gmailService),
                eq(email),
                eq("Password Reset Request"),
                argThat(content -> content.contains("Click the link to reset your password:") &&
                        content.contains("token=" + token))
        );
    }

    @Test
    void shouldThrowsEmailSendingExceptionWhenPasswordResetEmailFailure() throws Exception {
        String resetPath = "/reset";
        User user = buildUser(EMAIL);

        String token = "dummyToken";

        when(userSearchService.findByEmail(EMAIL)).thenReturn(user);
        when(jwtUtil.generateResetToken(user)).thenReturn(token);
        ReflectionTestUtils.setField(passwordResetService, "frontendBaseUrl", frontendBaseUrl);

        Gmail gmailService = mock(Gmail.class);
        when(gmailQuickstart.getGmailService()).thenReturn(gmailService);

        doThrow(new RuntimeException("Gmail error"))
                .when(gmailQuickstart)
                .sendEmail(any(Gmail.class), anyString(), anyString(), anyString());

        assertThrows(EmailSendingException.class, () ->
                passwordResetService.sendPasswordResetEmail(EMAIL, resetPath)
        );
    }
 }