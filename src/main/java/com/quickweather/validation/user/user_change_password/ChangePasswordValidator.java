package com.quickweather.validation.user.user_change_password;

import com.quickweather.domain.user.User;
import com.quickweather.dto.user.user_auth.ChangePasswordRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static java.util.Objects.isNull;

public abstract class ChangePasswordValidator {
    private ChangePasswordValidator next;

    public static ChangePasswordValidator link(ChangePasswordValidator first, ChangePasswordValidator... chain) {
        ChangePasswordValidator head = first;
        for (ChangePasswordValidator nextInChain: chain) {
            head.next = nextInChain;
            head = nextInChain;
        }
        return first;
    }

    public abstract void validate(User user, ChangePasswordRequest request, PasswordEncoder encoder);

    protected void validateNext(User user, ChangePasswordRequest request, PasswordEncoder encoder) {
        if (isNull(next)) {
            return;
        }
        next.validate(user, request, encoder);
    }
}
