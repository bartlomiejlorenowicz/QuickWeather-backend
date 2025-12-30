package com.quickweather.service.weather;

import com.quickweather.dto.weatherDtos.airpollution.AirPollutionResponseDto;
import com.quickweather.dto.weatherDtos.forecast.HourlyForecastResponseDto;
import com.quickweather.dto.weatherDtos.forecast.WeatherForecastDailyResponseDto;
import com.quickweather.dto.weatherDtos.weather.response.WeatherResponse;
import com.quickweather.dto.weatherDtos.weather.response.WeatherByZipCodeResponseDto;

public interface OpenWeatherMapService {

    WeatherResponse getCurrentWeatherByCity(String city);

    WeatherByZipCodeResponseDto getCurrentWeatherByZipcode(String zipcode, String countryCode);

    HourlyForecastResponseDto get5DaysForecastEvery3Hours(String city);

    AirPollutionResponseDto getAirPollutionByCoordinates(double lat, double lon);

    WeatherForecastDailyResponseDto getWeatherForecastByCityAndDays(String city, int cnt);

}
