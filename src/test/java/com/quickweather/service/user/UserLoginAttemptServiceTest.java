package com.quickweather.service.user;

import com.quickweather.admin.SecurityEventType;
import com.quickweather.domain.user.User;
import com.quickweather.repository.UserRepository;
import com.quickweather.service.admin.SecurityEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLoginAttemptServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityEventService securityEventService;

    @Mock
    private Clock clock;

    @InjectMocks
    private UserLoginAttemptService userLoginAttemptService;


    private User testUser;

    @BeforeEach
    void SetUp() {
        Instant fixedTime = Instant.parse("2025-03-27T11:00:00Z");
        lenient().when(clock.instant()).thenReturn(fixedTime);
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        testUser = new User();
        testUser.setEmail("bartek@wp.pl");
        testUser.setFailedAttempts(0);
        testUser.setEnabled(true);
    }

    @Test
    void shouldReturnThatUserIsNotLocking() {
        when(userRepository.findByEmail("bartek@wp.pl")).thenReturn(Optional.of(testUser));

        userLoginAttemptService.incrementFailedAttempts("bartek@wp.pl");

        assertEquals(1, testUser.getFailedAttempts());
        assertTrue(testUser.isEnabled());
        verify(userRepository).save(testUser);
    }

    @Test
    void shouldReturnLockingUser() {
        testUser.setFailedAttempts(4);

        when(userRepository.findByEmail("bartek@wp.pl")).thenReturn(Optional.of(testUser));

        userLoginAttemptService.incrementFailedAttempts("bartek@wp.pl");

        assertEquals(5, testUser.getFailedAttempts());
        assertFalse(testUser.isEnabled());
        LocalDateTime expected = LocalDateTime.ofInstant(clock.instant(), clock.getZone()).plusMinutes(15);
        assertEquals(expected, testUser.getLockUntil());
        verify(securityEventService).logEvent("bartek@wp.pl", SecurityEventType.MULTIPLE_LOGIN_ATTEMPTS, "system");
        verify(userRepository).save(testUser);
    }

    @Test
    void shouldResetFailedAttempts() {
        testUser.setFailedAttempts(3);
        testUser.setEnabled(false);
        testUser.setLockUntil(LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
        when(userRepository.findByEmail("bartek@wp.pl")).thenReturn(Optional.of(testUser));

        userLoginAttemptService.resetFailedAttempts("bartek@wp.pl");

        assertEquals(0, testUser.getFailedAttempts());
        assertTrue(testUser.isEnabled());
        verify(userRepository).save(testUser);
    }

    @Test
    void shouldReturnAccountStillLocked() {
        testUser.setEnabled(false);
        testUser.setLockUntil(LocalDateTime.ofInstant(clock.instant(), clock.getZone()).plusMinutes(10));

        boolean accountLocked = userLoginAttemptService.isAccountLocked(testUser);
        assertTrue(accountLocked);
    }

    @Test
    void shouldReturnAccountUnlockAfterTimeExpired() {
        Instant pastTime = Instant.parse("2025-03-27T10:00:00Z");
        testUser.setEnabled(false);
        testUser.setLockUntil(LocalDateTime.ofInstant(pastTime, ZoneId.of("UTC")));

        boolean accountLocked = userLoginAttemptService.isAccountLocked(testUser);
        assertFalse(accountLocked);
        assertTrue(testUser.isEnabled());
        assertEquals(0, testUser.getFailedAttempts());
        assertNull(testUser.getLockUntil());
        verify(userRepository).save(testUser);
    }
}