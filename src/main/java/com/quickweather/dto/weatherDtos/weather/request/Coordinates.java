package com.quickweather.dto.weatherDtos.weather.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Coordinates {
    private final String latStr;
    private final String lonStr;
    private Double lat;
    private Double lon;

    public Coordinates(String latStr, String lonStr) {
        this.latStr = latStr;
        this.lonStr = lonStr;
    }
}
