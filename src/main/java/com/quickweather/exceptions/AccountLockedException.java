package com.quickweather.exceptions;

import java.time.LocalDateTime;

public class AccountLockedException extends RuntimeException {

    private final LocalDateTime lockUntil;

    public AccountLockedException(LocalDateTime lockUntil) {
        super("Account is locked");
        this.lockUntil = lockUntil;
    }

    public LocalDateTime getLockUntil() {
        return lockUntil;
    }
}