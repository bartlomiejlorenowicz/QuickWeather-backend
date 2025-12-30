package com.quickweather.service.token;
import static org.junit.jupiter.api.Assertions.*;
import com.quickweather.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenValidationServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TokenValidationService tokenValidationService;

    @Test
    void shouldThrowUnauthorizedWhenTokenIsInvalid() {
        String token = "invalid-token";

        when(jwtUtil.validateResetToken(token)).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> tokenValidationService.validateResetTokenOrThrow(token)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Invalid or expired token"));
    }

    @Test
    void shouldThrowForbiddenWhenTokenTypeIsInvalid() {
        String token = "valid-token";

        when(jwtUtil.validateResetToken(token)).thenReturn(true);
        when(jwtUtil.extractResetTokenForType(token)).thenReturn("access-token");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> tokenValidationService.validateResetTokenOrThrow(token)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Invalid token type"));
    }

    @Test
    void shouldPassValidationWhenTokenIsValidAndCorrectType() {
        String token = "valid-reset-token";

        when(jwtUtil.validateResetToken(token)).thenReturn(true);
        when(jwtUtil.extractResetTokenForType(token)).thenReturn("reset-password");

        assertDoesNotThrow(() ->
                tokenValidationService.validateResetTokenOrThrow(token)
        );

        verify(jwtUtil).validateResetToken(token);
        verify(jwtUtil).extractResetTokenForType(token);
    }
}
