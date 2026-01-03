package com.quickweather.validation.user.user_creation;

import com.quickweather.dto.user.RegisterUserRequest;
import com.quickweather.dto.user.UserDto;
import com.quickweather.exceptions.UserErrorType;
import com.quickweather.exceptions.UserValidationException;

import static java.util.Objects.isNull;

public class UserPasswordValidator extends Validator {

    private static final String PASSWORD_SPECIAL_CHARACTER = ".*[!@#$%^&*(),.?\\\":{}|<>].*";

    @Override
    public void validate(RegisterUserRequest registerUserRequest) {

        String password = registerUserRequest.getPassword();

        if (isNull(password)) {
            throw new UserValidationException(UserErrorType.INVALID_PASSWORD, "password is null");
        }
        boolean passwordTooShort = password.length() < 8;
        if (passwordTooShort) {
            throw new UserValidationException(UserErrorType.INVALID_PASSWORD, "password must be minimum 8 characters long");
        }
        boolean passwordDoesNotHaveSpecialCharacter = !password.matches(PASSWORD_SPECIAL_CHARACTER);
        if (passwordDoesNotHaveSpecialCharacter) {
            throw new UserValidationException(UserErrorType.INVALID_PASSWORD, "password does not contain a special character");
        }
        validateNext(registerUserRequest);
    }
}
