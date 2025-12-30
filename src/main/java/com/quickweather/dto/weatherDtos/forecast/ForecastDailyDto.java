package com.quickweather.dto.weatherDtos.forecast;

import com.quickweather.dto.weatherDtos.weather.request.Main;
import com.quickweather.dto.weatherDtos.weather.request.Weather;
import com.quickweather.dto.weatherDtos.weather.request.Wind;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class ForecastDailyDto {
    private Main main;
    private List<Weather> weather;
    private Wind wind;
    private int visibility;

}
