package com.quickweather.validation.location;

import com.quickweather.dto.weatherDtos.weather.request.Coordinates;
import com.quickweather.exceptions.WeatherErrorType;
import com.quickweather.exceptions.WeatherServiceException;

public class EmptyCoordinatesValidator extends CoordinatesValidator {
    @Override
    public void validate(Coordinates coordinates) {
        if (coordinates.getLatStr() == null || coordinates.getLonStr() == null) {
            throw new WeatherServiceException(WeatherErrorType.INVALID_COORDINATES, "coordinate Latitude is null");
        }
        if (coordinates.getLatStr().trim().isEmpty() || coordinates.getLonStr().trim().isEmpty()) {
            throw new WeatherServiceException(WeatherErrorType.INVALID_COORDINATES, "coordinate Longitude is empty");
        }

        validateNext(coordinates);
    }
}
