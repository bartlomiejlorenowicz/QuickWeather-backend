package com.quickweather.service.user;

import com.quickweather.domain.user.User;
import com.quickweather.dto.apiResponse.LoginResponse;
import com.quickweather.dto.user.login.LoginRequest;
import com.quickweather.exceptions.AccountLockedException;
import com.quickweather.repository.UserRepository;
import com.quickweather.security.JwtUtil;
import com.quickweather.security.userdatails.CustomUserDetails;
import com.quickweather.service.admin.SecurityEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserLoginAttemptService userLoginAttemptService;
    private final SecurityEventService securityEventService;

    public LoginResponse login(LoginRequest loginRequest) {
        validateLoginRequest(loginRequest);

        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
        if (userOpt.isEmpty()) {
            log.error("Authentication failed for user: {}", loginRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        User user = userOpt.get();
        if (userLoginAttemptService.isAccountLocked(user)) {
            throw new AccountLockedException(user.getLockUntil());
        }

        try {
            Authentication authentication = authenticateUser(loginRequest);
            userLoginAttemptService.resetFailedAttempts(loginRequest.getEmail());
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            Map<String, Object> tokenMap = jwtUtil.generateToken(customUserDetails, customUserDetails.getUuid());
            return LoginResponse.fromTokenMap(tokenMap, customUserDetails);
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", loginRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    private void validateLoginRequest(LoginRequest loginRequest) {
        if (loginRequest.getEmail().isBlank() || loginRequest.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password must not be blank");
        }
    }

    private Authentication authenticateUser(LoginRequest loginRequest) throws AuthenticationException {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
    }

}
