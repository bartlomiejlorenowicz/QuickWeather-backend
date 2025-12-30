package com.quickweather.service.user;

import com.quickweather.admin.SecurityEventType;
import com.quickweather.domain.user.User;
import com.quickweather.repository.UserRepository;
import com.quickweather.service.admin.SecurityEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserLoginAttemptService {

    private final UserRepository userRepository;
    private final SecurityEventService securityEventService;
    private final Clock clock;

    public void incrementFailedAttempts(String email) {
        updateUserByEmail(email, user -> {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= 5) {
                user.setEnabled(false);
                user.setLockUntil(LocalDateTime.now(clock).plusMinutes(15));
                log.info("user exceeded login attempts: {}", email);
                securityEventService.logEvent(email, SecurityEventType.MULTIPLE_LOGIN_ATTEMPTS, "system");
                log.info("Incrementing failed attempts for user {}, current attempts={}", email, user.getFailedAttempts());
            }
        });
    }

    public void resetFailedAttempts(String email) {
        updateUserByEmail(email, user -> {
            user.setFailedAttempts(0);
            user.setEnabled(true);
            user.setUpdatedAt(null);
            user.setLockUntil(null);
        });
    }

    public boolean isAccountLocked(User user) {
        if (!user.isEnabled() && user.getLockUntil() != null) {
            if (user.getLockUntil().isBefore(LocalDateTime.now(clock))) {
                user.setEnabled(true);
                user.setFailedAttempts(0);
                user.setLockUntil(null);
                userRepository.save(user);
                return false;
            }
            return true;
        }
        return false;
    }

    private void updateUserByEmail(String email, Consumer<User> userUpdater) {
        userRepository.findByEmail(email).ifPresent(user -> {
            userUpdater.accept(user);
            userRepository.save(user);
        });
    }

}
