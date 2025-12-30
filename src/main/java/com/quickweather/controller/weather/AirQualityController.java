package com.quickweather.controller.weather;

import com.quickweather.dto.weatherDtos.airpollution.AirPollutionResponseDto;
import com.quickweather.service.weather.OpenWeatherServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weather")
@Validated
@RequiredArgsConstructor
public class AirQualityController {

    private final OpenWeatherServiceImpl currentWeatherService;

    @GetMapping("/air-quality")
    public AirPollutionResponseDto getAirPollution(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {
        return currentWeatherService.getAirPollution(city, lat, lon);
    }

}
