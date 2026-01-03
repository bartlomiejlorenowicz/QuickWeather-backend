package com.quickweather.validation.user.user_creation;

import com.quickweather.dto.user.RegisterUserRequest;
import com.quickweather.dto.user.UserDto;

import static java.util.Objects.isNull;

public abstract class Validator {
    private Validator next;

    public static Validator link(Validator first, Validator... chain) {
        Validator head = first;
        for (Validator nextInChain : chain) {
            head.next = nextInChain;
            head = nextInChain;
        }
        return first;
    }

    public abstract void validate(RegisterUserRequest registerUserRequest);

    protected void validateNext(RegisterUserRequest registerUserRequest) {
        if (isNull(next)) {
            return;
        }
        next.validate(registerUserRequest);
    }
}
