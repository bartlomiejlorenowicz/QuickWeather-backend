package com.quickweather.dto.weatherDtos.weather.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherByZipCodeResponseDto {

    @JsonProperty("weather")
    private Weather[] weatherArray;

    private Main main;

    private int visibility;

    private Wind wind;

    public String getWeatherMain() {
        return weatherArray != null && weatherArray.length > 0 ? weatherArray[0].getMain() : null;
    }

    public String getWeatherDescription() {
        return weatherArray != null && weatherArray.length > 0 ? weatherArray[0].getDescription() : null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Weather {
        private int id;
        private String main;
        private String description;
        private String icon;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main {
        private double temp;
        @JsonProperty("feels_like")
        private double feelsLike;
        @JsonProperty("temp_min")
        private double tempMin;
        @JsonProperty("temp_max")
        private double tempMax;
        private int pressure;
        private int humidity;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Wind {
        private double speed;
        private int deg;
    }
}