package com.quickweather.validation.user.user_creation;

import com.quickweather.dto.user.RegisterUserRequest;
import com.quickweather.dto.user.UserDto;
import com.quickweather.exceptions.UserErrorType;
import com.quickweather.exceptions.UserValidationException;

import static java.util.Objects.isNull;

public class UserFirstNameValidator extends Validator {

    @Override
    public void validate(RegisterUserRequest registerUserRequest) {
        String firstName = registerUserRequest.getFirstName();
        if (isNull(firstName)) {
            throw new UserValidationException(UserErrorType.INVALID_FIRST_NAME, "first name is null");
        }

        firstName = firstName.trim();

        if (firstName.isEmpty()) {
            throw new UserValidationException(UserErrorType.INVALID_FIRST_NAME, "first name cannot be empty or only whitespace");
        }

        if (firstName.length() < 2) {
            throw new UserValidationException(UserErrorType.INVALID_FIRST_NAME, "first name must have at least 2 letters");
        }
        if (firstName.length() > 30) {
            throw new UserValidationException(UserErrorType.INVALID_FIRST_NAME, "first name must have max 30 letters");
        }

        if (!firstName.matches("^[A-Za-zĄąĆćĘęŁłŃńÓóŚśŹźŻż\\-]+$")) {
            throw new UserValidationException(UserErrorType.INVALID_FIRST_NAME, "first name contains invalid characters");
        }

        validateNext(registerUserRequest);
    }
}

