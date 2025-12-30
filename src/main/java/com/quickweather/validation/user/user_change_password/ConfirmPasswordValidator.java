package com.quickweather.validation.user.user_change_password;

import com.quickweather.domain.user.User;
import com.quickweather.dto.user.user_auth.ChangePasswordRequest;
import com.quickweather.exceptions.UserChangePasswordValidationException;
import com.quickweather.exceptions.UserErrorType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Getter
public class ConfirmPasswordValidator extends ChangePasswordValidator {
    @Override
    public void validate(User user, ChangePasswordRequest request, PasswordEncoder encoder) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.error("Passwords do not match.");
            throw new UserChangePasswordValidationException(UserErrorType.INVALID_CURRENT_PASSWORD, "Passwords do not match.");
        }
        validateNext(user, request, encoder);
    }
}
