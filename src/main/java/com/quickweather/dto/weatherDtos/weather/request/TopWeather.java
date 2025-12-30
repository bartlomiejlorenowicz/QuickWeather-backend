package com.quickweather.dto.weatherDtos.weather.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopWeather {
    private String label;
    private int count;
}
