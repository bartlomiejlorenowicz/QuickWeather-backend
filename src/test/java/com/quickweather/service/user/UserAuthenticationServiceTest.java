package com.quickweather.service.user;

import com.quickweather.domain.user.User;
import com.quickweather.dto.apiResponse.LoginResponse;
import com.quickweather.dto.user.login.LoginRequest;
import com.quickweather.exceptions.AccountLockedException;
import com.quickweather.repository.UserRepository;
import com.quickweather.security.JwtUtil;
import com.quickweather.security.userdatails.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserLoginAttemptService loginAttemptService;

    @InjectMocks
    private UserAuthenticationService userAuthenticationService;

    String email = "bartek123@wp.pl";

    @Test
    void shouldAuthenticatedUser() {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("Bartek123!");

        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(loginAttemptService.isAccountLocked(user)).thenReturn(false);

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails customUserDetails = mock(CustomUserDetails.class);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);

        UUID uuid = UUID.randomUUID();
        when(customUserDetails.getUuid()).thenReturn(uuid);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        Map<String, Object> tokenMap = Map.of(
                "token", "dummyToken",
                "expiresAt", new Date(System.currentTimeMillis() + 3600000),
                "email", email
        );
        when(jwtUtil.generateToken(customUserDetails, uuid)).thenReturn(tokenMap);

        LoginResponse response = userAuthenticationService.login(request);

        assertNotNull(response);
        assertEquals("dummyToken", response.getToken());
        verify(loginAttemptService).resetFailedAttempts(email);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("Bartek123!");

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userAuthenticationService.login(request));
        assertTrue(exception.getMessage().contains("Invalid credentials"));
    }

    @Test
    void shouldThrowExceptionWhenAccountLocked() {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("Bartek123!");

        User user = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(loginAttemptService.isAccountLocked(user)).thenReturn(true);

        AccountLockedException exception =
                assertThrows(AccountLockedException.class, () ->
                        userAuthenticationService.login(request)
                );

        assertEquals("Account is locked", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationFails() {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("Bartek123!");

        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(loginAttemptService.isAccountLocked(user)).thenReturn(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userAuthenticationService.login(request));
        assertTrue(exception.getMessage().contains("Invalid credentials"));
    }

    @Test
    void shouldThrowExceptionWhenLoginRequestIsInvalid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userAuthenticationService.login(request));
        assertTrue(exception.getReason().contains("must not be blank"));
    }
}