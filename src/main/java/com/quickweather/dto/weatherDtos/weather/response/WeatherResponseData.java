package com.quickweather.dto.weatherDtos.weather.response;

import com.quickweather.domain.weather.ApiSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WeatherResponseData {
    private String city;
    private String countryCode;
    private ApiSource apiSource;
    private String responseJson;
    private String requestJson;
}
