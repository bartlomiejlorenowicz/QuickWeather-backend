package com.quickweather.validation.location;

import com.quickweather.dto.weatherDtos.weather.request.Coordinates;
import com.quickweather.exceptions.WeatherErrorType;
import com.quickweather.exceptions.WeatherServiceException;

public class CoordinatesRangeValidator extends CoordinatesValidator {
    @Override
    public void validate(Coordinates coordinates) {
        double lat = coordinates.getLat();
        double lon = coordinates.getLon();

        if (lat < -90 || lat > 90) {
            throw new WeatherServiceException(WeatherErrorType.INVALID_COORDINATES, "Latitude must be between -90 and 90");
        }
        if (lon < -180 || lon > 180) {
            throw new WeatherServiceException(WeatherErrorType.INVALID_COORDINATES, "Longitude must be between -180 and 180");
        }

        validateNext(coordinates);
    }
}
