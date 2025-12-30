package com.quickweather.service.user;

import com.quickweather.dto.user.user_auth.ChangePasswordRequest;
import com.quickweather.domain.user.User;
import com.quickweather.exceptions.UserErrorType;
import com.quickweather.exceptions.UserNotFoundException;
import com.quickweather.repository.UserRepository;
import com.quickweather.validation.user.user_change_password.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Provides functionality to change a user's password.
 * <p>
 * The process includes:
 * <ul>
 *   <li>Retrieving the user by email (throws 404 if the user is not found).</li>
 *   <li>Validating the current password (throws 401 if incorrect).</li>
 *   <li>Checking the new password against the confirmation field (throws 400 if they differ).</li>
 *   <li>Ensuring the new password is not the same as the current one.</li>
 *   <li>Encoding and saving the new password.</li>
 *   <li>Clearing the security context to force re-authentication.</li>
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordService {

    private final UserSearchService userSearchService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ChangePasswordValidatorChain validatorChain;

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userSearchService.findByEmail(email);
        if (user == null) {
            log.info("User not found.");
            throw new UserNotFoundException(UserErrorType.USER_NOT_FOUND, "User not found.");
        }

        ChangePasswordValidator validator = validatorChain.buildChain();
        validator.validate(user, request, passwordEncoder);

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        SecurityContextHolder.clearContext();
        log.info("Password changed successfully for user: {}. Security context cleared.", email);
    }

}
