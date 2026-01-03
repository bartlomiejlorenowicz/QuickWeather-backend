package com.quickweather.validation.user.user_creation;

import com.quickweather.dto.user.RegisterUserRequest;
import com.quickweather.dto.user.UserDto;
import com.quickweather.exceptions.UserErrorType;
import com.quickweather.exceptions.UserValidationException;

import static java.util.Objects.isNull;

public class UserPhoneNumberValidator extends Validator {

    @Override
    public void validate(RegisterUserRequest registerUserRequest) {
        String phoneNumber = registerUserRequest.getPhoneNumber();
        if (isNull(phoneNumber)) {
            throw new UserValidationException(UserErrorType.INVALID_PHONE_NUMBER, "phone number is null");
        }
        boolean phoneNumberTooShort = phoneNumber.length() < 10;
        if (phoneNumberTooShort) {
            throw new UserValidationException(UserErrorType.INVALID_PHONE_NUMBER, "phone number must have at least 10 digits");
        }
        boolean phoneNumberTooLong = phoneNumber.length() > 15;
        if (phoneNumberTooLong) {
            throw new UserValidationException(UserErrorType.INVALID_PHONE_NUMBER, "phone number must have maximum 15 digits");
        }
        validateNext(registerUserRequest);
    }
}
