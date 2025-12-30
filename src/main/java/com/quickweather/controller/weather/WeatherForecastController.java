package com.quickweather.controller.weather;

import com.quickweather.dto.weatherDtos.forecast.WeatherForecastDailyResponseDto;
import com.quickweather.dto.weatherDtos.weather.request.SimpleForecastDto;
import com.quickweather.service.weather.OpenWeatherServiceImpl;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/weather/forecast")
@Validated
@RequiredArgsConstructor
public class WeatherForecastController {

    private final OpenWeatherServiceImpl currentWeatherService;

    @GetMapping
    public List<SimpleForecastDto> getForecast(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {
        return currentWeatherService.getSimpleForecast(city, lat, lon);
    }

    @GetMapping("/daily")
    public WeatherForecastDailyResponseDto getWeatherForecastByCityAndDays(
            @RequestParam @NotBlank(message = "City name cannot be blank") String city,
            @RequestParam @Min(value = 1, message = "Count must be at least 1")
            @Max(value = 16, message = "Count cannot be more than 16") int cnt) {
        return currentWeatherService.getWeatherForecastByCityAndDays(city, cnt);
    }
}
