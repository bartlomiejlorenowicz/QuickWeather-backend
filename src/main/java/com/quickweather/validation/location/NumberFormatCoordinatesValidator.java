package com.quickweather.validation.location;

import com.quickweather.dto.weatherDtos.weather.request.Coordinates;
import com.quickweather.exceptions.WeatherErrorType;
import com.quickweather.exceptions.WeatherServiceException;

public class NumberFormatCoordinatesValidator extends CoordinatesValidator {
    @Override
    public void validate(Coordinates coordinates) {

        try {
            double lat = Double.parseDouble(coordinates.getLatStr());
            double lon = Double.parseDouble(coordinates.getLonStr());
            coordinates.setLat(lat);
            coordinates.setLon(lon);
        } catch (NumberFormatException e) {
            throw new WeatherServiceException(WeatherErrorType.INVALID_COORDINATES, "Latitude and Longitude must be valid numbers");
        }

        validateNext(coordinates);
    }
}
