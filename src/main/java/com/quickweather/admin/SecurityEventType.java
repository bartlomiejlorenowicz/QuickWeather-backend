package com.quickweather.admin;

public enum SecurityEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    AUTHORIZATION_FAILURE,
    ACCOUNT_CREATED,
    ACCOUNT_DELETED,
    MULTIPLE_LOGIN_ATTEMPTS,
    ACCOUNT_LOCKED
}
