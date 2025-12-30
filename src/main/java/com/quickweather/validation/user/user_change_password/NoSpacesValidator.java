package com.quickweather.validation.user.user_change_password;

import com.quickweather.domain.user.User;
import com.quickweather.dto.user.user_auth.ChangePasswordRequest;
import com.quickweather.exceptions.UserChangePasswordValidationException;
import com.quickweather.exceptions.UserErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class NoSpacesValidator extends ChangePasswordValidator {

    @Override
    public void validate(User user, ChangePasswordRequest request, PasswordEncoder encoder) {
        String newPassword = request.getNewPassword();
        if (newPassword.contains(" ")) {
            log.error("New password must not contain spaces.");
            throw new UserChangePasswordValidationException(
                    UserErrorType.INVALID_NEW_PASSWORD,
                    "New password must not contain spaces."
            );
        }
        // Przekazanie kontroli do kolejnego walidatora, jeśli istnieje
        validateNext(user, request, encoder);
    }
}
