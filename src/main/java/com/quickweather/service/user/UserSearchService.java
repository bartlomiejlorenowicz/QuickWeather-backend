package com.quickweather.service.user;

import com.quickweather.domain.user.User;
import com.quickweather.exceptions.UserErrorType;
import com.quickweather.exceptions.UserNotFoundException;
import com.quickweather.exceptions.UserValidationException;
import com.quickweather.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserSearchService {
    private final UserRepository userRepository;

    public UserSearchService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Finds a user by ID, throws an exception if not found.
     */
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserValidationException(UserErrorType.USER_NOT_FOUND, "User not found by ID: " + userId)
                );
    }

    /**
     * Finds a user by email, throws if not found.
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(UserErrorType.USER_NOT_FOUND, "User not found with email: " + email));
    }


}
