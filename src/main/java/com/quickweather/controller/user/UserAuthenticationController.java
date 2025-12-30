package com.quickweather.controller.user;

import com.quickweather.dto.apiResponse.ApiResponse;
import com.quickweather.dto.apiResponse.LoginResponse;
import com.quickweather.dto.apiResponse.OperationType;
import com.quickweather.dto.user.login.LoginRequest;
import com.quickweather.dto.user.user_auth.ChangePasswordRequest;
import com.quickweather.service.user.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user/auth")
public class UserAuthenticationController {

    private final PasswordService passwordService;
    private final UserAuthenticationService userAuthenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = userAuthenticationService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    //ApiResponse - bazowa
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        log.info("Authenticated user: {}", authentication.getName());
        String email = authentication.getName();

        passwordService.changePassword(email, request);

        ApiResponse apiResponse = ApiResponse.buildApiResponse("Password changed successfully. Please log in again.", OperationType.CHANGE_PASSWORD);

        return ResponseEntity.ok(apiResponse);
    }

}
