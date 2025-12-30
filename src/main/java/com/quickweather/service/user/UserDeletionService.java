package com.quickweather.service.user;

import com.quickweather.admin.SecurityEventType;
import com.quickweather.domain.user.User;
import com.quickweather.exceptions.UserErrorType;
import com.quickweather.exceptions.UserNotFoundException;
import com.quickweather.repository.UserRepository;
import com.quickweather.service.admin.SecurityEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDeletionService {

    private final UserRepository userRepository;
    private final SecurityEventService securityEventService;

    public void deleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            userRepository.delete(user);
            log.info("User account deleted: {}", user.getEmail());

            String ipAddress = securityEventService.getClientIpAddress();
            securityEventService.logEvent(user.getEmail(), SecurityEventType.ACCOUNT_DELETED, ipAddress);
        } else {
            log.error("User with id {} not found", userId);
            throw new UserNotFoundException(UserErrorType.USER_NOT_FOUND, "User not found");
        }
    }
}
