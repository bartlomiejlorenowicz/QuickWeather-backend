package com.quickweather.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String MESSAGE = "message";
    private static final String ERROR_TYPE = "errorType";
    private static final String TIMESTAMP = "timestamp";

    @ExceptionHandler(UserValidationException.class)
    public ResponseEntity<Map<String, String>> handlerUserValidationException(UserValidationException ex) {
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE, ex.getMessage());
        response.put(ERROR_TYPE, ex.getUserErrorType().name());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex.getUserErrorType() == UserErrorType.EMAIL_ALREADY_EXISTS) {
            status = HttpStatus.CONFLICT;
        }
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(WeatherServiceException.class)
    public ResponseEntity<Map<String, String>> handleWeatherServiceException(WeatherServiceException ex) {
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE, ex.getMessage());
        response.put(ERROR_TYPE, ex.getWeatherErrorType().name());
        response.put(TIMESTAMP, LocalDateTime.now().toString());

        HttpStatus status = mapErrorTypeToHttpStatus(ex.getWeatherErrorType());
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ConstraintViolationException ex) {
        Map<String, String> response = new HashMap<>();

        String simplifiedMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Validation error");

        response.put(MESSAGE, simplifiedMessage);
        response.put(ERROR_TYPE, WeatherErrorType.BAD_REQUEST.name());
        response.put(TIMESTAMP, LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE, "Required request parameter '" + ex.getParameterName() + "' is missing");
        response.put(ERROR_TYPE, WeatherErrorType.BAD_REQUEST.name());
        response.put(TIMESTAMP, LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        Map<String, String> response = new HashMap<>();
        String paramName = Optional.of(ex.getName()).orElse("unknown");
        String paramType = Optional.ofNullable(ex.getRequiredType())
                .map(Class::getSimpleName)
                .orElse("unknown");

        response.put(MESSAGE, "Failed to convert value of parameter '" + paramName + "' to required type '" + paramType + "'");
        response.put(ERROR_TYPE, WeatherErrorType.BAD_REQUEST.name());
        response.put(TIMESTAMP, LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE, ex.getMessage());
        response.put("userId", ex.getUserId());
        response.put(TIMESTAMP, LocalDateTime.now().toString());
        response.put(ERROR_TYPE, "USER_NOT_FOUND");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<Map<String, String>> handleEmailSendingException(EmailSendingException ex) {
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE, ex.getMessage());
        response.put(ERROR_TYPE, "EMAIL_SENDING_ERROR");
        response.put(TIMESTAMP, LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(UserChangePasswordValidationException.class)
    public ResponseEntity<Map<String, String>> handleUserChangePasswordValidationException(UserChangePasswordValidationException ex) {
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE, ex.getMessage());
        response.put(ERROR_TYPE, ex.getUserErrorType() != null
                ? ex.getUserErrorType().name()
                : UserErrorType.INVALID_CURRENT_PASSWORD.name());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountLocked(AccountLockedException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Account is locked. Please try again later.");
        response.put("errorType", "ACCOUNT_LOCKED");
        response.put("lockUntil", ex.getLockUntil());
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity
                .status(HttpStatus.LOCKED)
                .body(response);
    }

    private HttpStatus mapErrorTypeToHttpStatus(WeatherErrorType weatherErrorType) {

        return switch (weatherErrorType) {
            case INVALID_API_KEY -> HttpStatus.UNAUTHORIZED;
            case DATA_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case WEATHER_DATA_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case EXTERNAL_API_ERROR -> HttpStatus.BAD_GATEWAY;
            case INVALID_COORDINATES, INVALID_CITY_NAME, INVALID_ZIP_CODE, SERIALIZATION_ERROR-> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
