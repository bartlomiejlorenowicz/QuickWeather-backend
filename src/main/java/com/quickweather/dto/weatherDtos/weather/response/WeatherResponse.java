package com.quickweather.dto.weatherDtos.weather.response;

import com.quickweather.dto.weatherDtos.location.Coord;
import com.quickweather.dto.weatherDtos.location.SysDto;
import com.quickweather.dto.weatherDtos.weather.request.Main;
import com.quickweather.dto.weatherDtos.weather.request.Weather;
import com.quickweather.dto.weatherDtos.weather.request.Wind;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WeatherResponse {

    private Main main;
    private List<Weather> weather;
    private String name;
    private Coord coord;
    private int visibility;
    private Wind wind;
    private SysDto sys;

}
