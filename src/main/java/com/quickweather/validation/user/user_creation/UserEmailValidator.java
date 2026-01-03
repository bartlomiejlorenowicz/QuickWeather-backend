package com.quickweather.validation.user.user_creation;

import com.quickweather.dto.user.RegisterUserRequest;
import com.quickweather.dto.user.UserDto;
import com.quickweather.exceptions.UserErrorType;
import com.quickweather.exceptions.UserValidationException;
import com.quickweather.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class UserEmailValidator extends Validator {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    private final UserRepository userCreationRepository;

    public UserEmailValidator(UserRepository userCreationRepository) {
        this.userCreationRepository = userCreationRepository;
    }

    @Override
    public void validate(RegisterUserRequest registerUserRequest) {
        String email = registerUserRequest.getEmail();

        if (isNull(email)) {
            throw new UserValidationException(UserErrorType.INVALID_EMAIL, "email is null");
        }
        boolean incorrectEmail = !email.matches(EMAIL_REGEX);
        if (incorrectEmail) {
            throw new UserValidationException(UserErrorType.INVALID_EMAIL, "email is not valid");
        }
        boolean emailExistInDatabase = userCreationRepository.existsByEmail(email);
        if (emailExistInDatabase) {
            throw new UserValidationException(UserErrorType.EMAIL_ALREADY_EXISTS, "the given e-mail exists in the database");
        }

        validateNext(registerUserRequest);
    }
}
