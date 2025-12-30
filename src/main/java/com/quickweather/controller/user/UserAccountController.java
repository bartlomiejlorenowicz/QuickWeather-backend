package com.quickweather.controller.user;

import com.quickweather.dto.apiResponse.ApiResponse;
import com.quickweather.dto.apiResponse.OperationType;
import com.quickweather.dto.user.user_auth.ChangePasswordRequest;
import com.quickweather.service.user.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserAccountController {

    private final PasswordService passwordService;

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        log.info("Authenticated user: {}", authentication.getName());
        String email = authentication.getName();
        passwordService.changePassword(email, request);
        ApiResponse apiResponse = ApiResponse.buildApiResponse("Password changed successfully. Please log in again.", OperationType.CHANGE_PASSWORD);
        return ResponseEntity.ok(apiResponse);
    }
}
