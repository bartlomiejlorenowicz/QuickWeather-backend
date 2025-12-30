package com.quickweather.controller.weather;

import com.quickweather.dto.weatherDtos.accuweather.AccuWeatherResponse;
import com.quickweather.dto.weatherDtos.weather.response.WeatherResponse;
import com.quickweather.dto.weatherDtos.weather.response.WeatherByZipCodeResponseDto;
import com.quickweather.service.accuweather.AccuWeatherServiceImpl;
import com.quickweather.service.weather.OpenWeatherServiceImpl;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/weather")
@Validated
@RequiredArgsConstructor
public class WeatherController {

    private final OpenWeatherServiceImpl currentWeatherService;
    private final AccuWeatherServiceImpl accuWeatherService;

    @GetMapping("/city")
    public WeatherResponse getCurrentWeatherByCity(
            @RequestParam @NotBlank(message = "City name cannot be blank") String city) {
        return currentWeatherService.getCurrentWeatherByCity(city);
    }

    @GetMapping("/zipcode")
    public WeatherByZipCodeResponseDto getCurrentWeatherByZipcode(
            @RequestParam String zipcode,
            @RequestParam @Size(min = 2, max = 2, message = "Country code must be 2 letters") String countryCode) {
        return currentWeatherService.getCurrentWeatherByZipcode(zipcode, countryCode);
    }

    @GetMapping("/coordinate")
    public WeatherResponse getWeatherByCoordinates(@RequestParam String lat,
                                                   @RequestParam String lon) {
        return currentWeatherService.getCurrentWeatherByCoordinates(lat, lon);
    }

    @GetMapping("/postcode")
    public List<AccuWeatherResponse> getLocationByPostalCode(
            @RequestParam String postcode) {
        return accuWeatherService.getLocationByPostalCode(postcode);
    }
}
