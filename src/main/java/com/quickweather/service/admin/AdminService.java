package com.quickweather.service.admin;

import com.quickweather.domain.user.User;
import com.quickweather.dto.admin.AdminStatsResponse;
import com.quickweather.dto.admin.AdminUserDTO;
import com.quickweather.dto.weatherDtos.weather.request.CityLog;
import com.quickweather.dto.weatherDtos.weather.request.TopWeather;
import com.quickweather.dto.user.UserStatusRequest;
import com.quickweather.exceptions.UserErrorType;
import com.quickweather.exceptions.UserNotFoundException;
import com.quickweather.repository.ApiQueryLogRepository;
import com.quickweather.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ApiQueryLogRepository apiQueryLogRepository;
    private final EntityManager entityManager;
    private final Clock clock;

    public AdminStatsResponse getDashboardStats() {
        long activeUsers = userRepository.countByIsEnabledTrue();
        long totalUsers = userRepository.count();
        long inactiveUsers = totalUsers - activeUsers;

        AdminStatsResponse stats = new AdminStatsResponse();
        stats.setActiveUsers(activeUsers);
        stats.setTotalUsers(totalUsers);
        stats.setInactiveUsers(inactiveUsers);

        Pageable pageable = PageRequest.of(0, 10);
        Page<CityLog> cityLogsPage = apiQueryLogRepository.findTopCityLogs(pageable);
        List<CityLog> cityLogs = cityLogsPage.getContent();
        stats.setCityLogs(cityLogs);

        if (!cityLogs.isEmpty()) {
            CityLog topCityLog = cityLogs.get(0);
            TopWeather topWeather = new TopWeather();
            topWeather.setLabel(topCityLog.getCity());
            topWeather.setCount((int) topCityLog.getCount());
            stats.setTopWeather(topWeather);
        }

        return stats;
    }

    public Page<AdminUserDTO> getAllUsers(String email, Pageable pageable) {
        Page<User> userPage;
        if (email != null && !email.isEmpty()) {
            userPage = userRepository.findByEmail(email, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        return userPage.map(user -> new AdminUserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isEnabled()
        ));
    }

    public void updateUserStatus(Long userId, UserStatusRequest request) {
        log.info("Attempting to update status for user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserErrorType.USER_NOT_FOUND, "User not found"));

        log.info("User found: {}. Current status: {}. Proceeding with update.", user.getEmail(), user.isEnabled() ? "Enabled" : "Disabled");

        user.setEnabled(request.isEnabled());
        if (request.isEnabled()) {
            log.info("Enabling user: {}. Removing lock and resetting failed attempts.", user.getEmail());
            user.setLockUntil(null);
            user.setFailedAttempts(0);
            user.setLocked(false);
        } else {
            // Alternatywnie pozostawić enabled=false,
            log.info("Disabling user: {}. Setting future lock date.", user.getEmail());
            user.setLockUntil(LocalDateTime.now(clock).plusYears(100));
        }

        if (request.isUnblock()) {
            log.info("Unblocking user: {}. Resetting lock, failed attempts, and locked status.", user.getEmail());
            user.setLockUntil(null);
            user.setFailedAttempts(0);
            user.setLocked(false);
        }

        log.info("Saving updated user data for user with ID: {}", userId);
        userRepository.save(user);

        entityManager.getEntityManagerFactory().getCache().evict(User.class, user.getId());
        log.info("Evicted user data from cache for user with ID: {}", userId);
    }

    public void enableUser(Long userId) {
        UserStatusRequest request = new UserStatusRequest();
        request.setEnabled(true);
        request.setUnblock(true);
        updateUserStatus(userId, request);
    }

    public void disableUser(Long userId) {
        UserStatusRequest request = new UserStatusRequest();
        request.setEnabled(false);
        updateUserStatus(userId, request);
    }
}
