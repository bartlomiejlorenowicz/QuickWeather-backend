package com.quickweather.service.admin;

import com.quickweather.domain.user.User;
import com.quickweather.dto.admin.AdminStatsResponse;
import com.quickweather.dto.admin.AdminUserDTO;
import com.quickweather.dto.weatherDtos.weather.request.CityLog;
import com.quickweather.dto.weatherDtos.weather.request.TopWeather;
import com.quickweather.dto.user.UserStatusRequest;
import com.quickweather.exceptions.UserNotFoundException;
import com.quickweather.repository.ApiQueryLogRepository;
import com.quickweather.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.Cache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApiQueryLogRepository apiQueryLogRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Mock
    private Cache cache;

    @Mock
    private Clock clock;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        lenient().when(entityManager.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        lenient().when(entityManagerFactory.getCache()).thenReturn(cache);

        Instant fixed = Instant.parse("2025-03-31T12:00:00Z");
        lenient().when(clock.instant()).thenReturn(fixed);
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    @Test
    void shouldReturnDashboardStatsWithTopWeatherWhenCityLogsNoEmpty() {
        long activeUsers = 10L;
        long totalUsers = 20L;
        long inactiveUsers = totalUsers - activeUsers;
        when(userRepository.countByIsEnabledTrue()).thenReturn(activeUsers);
        when(userRepository.count()).thenReturn(totalUsers);

        CityLog cityLog = new CityLog();
        cityLog.setCity("London");
        cityLog.setCount(5L);
        Page<CityLog> cityLogPage = new PageImpl<>(List.of(cityLog));
        when(apiQueryLogRepository.findTopCityLogs(any(Pageable.class))).thenReturn(cityLogPage);

        AdminStatsResponse stats = adminService.getDashboardStats();

        assertEquals(activeUsers, stats.getActiveUsers());
        assertEquals(totalUsers, stats.getTotalUsers());
        assertEquals(inactiveUsers, stats.getInactiveUsers());

        assertNotNull(stats.getCityLogs());
        assertFalse(stats.getCityLogs().isEmpty());

        TopWeather topWeather = stats.getTopWeather();
        assertNotNull(topWeather);
        assertEquals("London", topWeather.getLabel());
        assertEquals(5, topWeather.getCount());
    }

    @Test
    void shouldReturnStatsWithoutTopWeatherWhenCityLogsEmpty() {
        long activeUsers = 15L;
        long totalUsers = 30L;
        when(userRepository.countByIsEnabledTrue()).thenReturn(activeUsers);
        when(userRepository.count()).thenReturn(totalUsers);

        Page<CityLog> cityLog = new PageImpl<>(Collections.emptyList());
        when(apiQueryLogRepository.findTopCityLogs(any(Pageable.class))).thenReturn(cityLog);

        AdminStatsResponse stats = adminService.getDashboardStats();

        assertEquals(activeUsers, stats.getActiveUsers());
        assertEquals(totalUsers, stats.getTotalUsers());
        assertEquals(totalUsers - activeUsers, stats.getInactiveUsers());
        assertNotNull(stats.getCityLogs());
        assertTrue(stats.getCityLogs().isEmpty());
        assertNull(stats.getTopWeather());
    }

    @Test
    void shouldReturnAllUsersSuccessfully() {
        String email = "bartek123@wp.pl";
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(1L);
        user.setFirstName("Bartek");
        user.setLastName("Loren");
        user.setEmail(email);
        user.setEnabled(true);

        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findByEmail(email, pageable)).thenReturn(userPage);

        Page<AdminUserDTO> result = adminService.getAllUsers(email, pageable);

        verify(userRepository, times(1)).findByEmail(email, pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        AdminUserDTO dto = result.getContent().get(0);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getFirstName(), dto.getFirstName());
        assertEquals(user.getEmail(), dto.getEmail());
        assertTrue(dto.isEnabled());
    }

    @Test
    void shouldReturnAllUsersWithoutEmailFilter() {
        String emailFilter = "";
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = new User();
        user1.setId(1L);
        user1.setFirstName("Alicja");
        user1.setLastName("Smith");
        user1.setEmail("alicja@wp.pl");
        user1.setEnabled(true);

        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Bob");
        user2.setLastName("Jones");
        user2.setEmail("bob@wp.pl");
        user2.setEnabled(false);

        Page<User> userPage = new PageImpl<>(List.of(user1, user2));
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<AdminUserDTO> result = adminService.getAllUsers(emailFilter, pageable);

        verify(userRepository, times(1)).findAll(pageable);
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        AdminUserDTO dto = result.getContent().get(0);
        assertEquals(user1.getId(), dto.getId());
        assertEquals(user1.getFirstName(), dto.getFirstName());
        assertEquals(user1.getEmail(), dto.getEmail());
        assertEquals(user1.isEnabled(), dto.isEnabled());
    }

    @Test
    void shouldEnableUserAndResetLockWhenRequestIsEnabledAndUnblockIsTrue() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("bartek123@wp.pl");
        user.setEnabled(false);
        user.setFailedAttempts(3);
        user.setLockUntil(LocalDateTime.now().plusDays(1));
        user.setLocked(true);

        UserStatusRequest request = new UserStatusRequest();
        request.setEnabled(true);
        request.setUnblock(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        adminService.updateUserStatus(userId, request);

        assertTrue(user.isEnabled());
        assertEquals(0, user.getFailedAttempts());
        assertNull(user.getLockUntil());
        assertFalse(user.isLocked());

        verify(userRepository).save(user);
        verify(cache).evict(User.class, userId);
    }

    @Test
    void shouldDisableUserAndSetLockWhenRequestIsEnabledFalse() {
        Long userId = 2L;
        User user = new User();
        user.setId(userId);
        user.setEmail("bartek@wp.pl");
        user.setEnabled(true);

        UserStatusRequest request  = new UserStatusRequest();
        request.setEnabled(false);
        request.setUnblock(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        adminService.updateUserStatus(userId, request);

        assertFalse(user.isEnabled());
        LocalDateTime expectedLockUntil = LocalDateTime.ofInstant(Instant.parse("2025-03-31T12:00:00Z"), ZoneId.of("UTC")).plusYears(100);
        assertEquals(expectedLockUntil, user.getLockUntil());

        verify(userRepository).save(user);
        verify(cache).evict(User.class, userId);
    }

    @Test
    void shouldThrowsUserNotFoundExceptionWhenUserDoesNotExist() {
        Long userId = 3L;
        UserStatusRequest request = new UserStatusRequest();
        request.setEnabled(true);
        request.setUnblock(true);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> adminService.updateUserStatus(userId, request)
        );
        assertTrue(exception.getMessage().contains("User not found"));
    }

}