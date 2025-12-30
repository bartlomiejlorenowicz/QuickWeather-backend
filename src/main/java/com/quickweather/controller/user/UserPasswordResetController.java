package com.quickweather.controller.user;

import com.quickweather.dto.apiResponse.ApiResponse;
import com.quickweather.dto.apiResponse.OperationType;
import com.quickweather.dto.token.TokenRequest;
import com.quickweather.dto.user.user_auth.EmailRequest;
import com.quickweather.dto.user.user_auth.SetNewPasswordRequest;
import com.quickweather.service.token.TokenValidationService;
import com.quickweather.service.user.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user/auth")
@RequiredArgsConstructor
@Slf4j
public class UserPasswordResetController {

    private final PasswordResetService passwordResetService;
    private final TokenValidationService tokenValidationService;


    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @Valid @RequestBody EmailRequest emailRequest
    ) {
        log.info("Forgot password requested for: {}", emailRequest.getEmail());

        passwordResetService.forgotPassword(emailRequest.getEmail());

        return ResponseEntity.ok(
                ApiResponse.buildApiResponse(
                        "Password reset link sent",
                        OperationType.FORGOT_PASSWORD
                )
        );
    }

    @PostMapping("/validate-reset-token")
    public ResponseEntity<ApiResponse> validateResetToken(
            @Valid @RequestBody TokenRequest tokenRequest
    ) {
        tokenValidationService.validateResetTokenOrThrow(tokenRequest.getToken());

        return ResponseEntity.ok(
                ApiResponse.buildApiResponse(
                        "Token is valid",
                        OperationType.VALIDATE_RESET_TOKEN
                )
        );
    }

    @PostMapping("/set-new-password")
    public ResponseEntity<ApiResponse> setNewPassword(
            @Valid @RequestBody SetNewPasswordRequest request
    ) {
        passwordResetService.resetPasswordUsingToken(request);

        return ResponseEntity.ok(
                ApiResponse.buildApiResponse(
                        "Password updated successfully",
                        OperationType.SET_NEW_PASSWORD
                )
        );
    }
}
