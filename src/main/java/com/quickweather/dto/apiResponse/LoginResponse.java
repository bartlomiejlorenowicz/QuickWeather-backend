package com.quickweather.dto.apiResponse;

import com.quickweather.security.userdatails.CustomUserDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Instant expiresAt;
    private String email;
    private Instant timestamp;
    private OperationType operationType;
    private String errorMessage;

    public static LoginResponse fromTokenMap(Map<String, Object> tokenMap, CustomUserDetails customUserDetails) {
        String token = (String) tokenMap.get("token");
        Instant expiresAt = ((Date) tokenMap.get("expiresAt")).toInstant();
        return new LoginResponse(
                token,
                expiresAt,
                customUserDetails.getEmail(),
                Instant.now(),
                OperationType.LOGIN,
                null
        );
    }

    public LoginResponse(OperationType operationType, String errorMessage) {
        this.operationType = operationType;
        this.errorMessage = errorMessage;
    }
}
