package com.quickweather.validation.user.user_change_password;

import com.quickweather.domain.user.User;
import com.quickweather.dto.user.user_auth.ChangePasswordRequest;
import com.quickweather.exceptions.UserChangePasswordValidationException;
import com.quickweather.exceptions.UserErrorType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Slf4j
public class NewPasswordDifferentValidator extends ChangePasswordValidator {
    @Override
    public void validate(User user, ChangePasswordRequest request, PasswordEncoder encoder) {
        if (encoder.matches(request.getNewPassword(), user.getPassword())) {
            log.error("New password must be different from the old password.");
            throw new UserChangePasswordValidationException(UserErrorType.INVALID_NEW_PASSWORD, "New password must be different from the old password.");
        }
        validateNext(user, request, encoder);
    }
}
