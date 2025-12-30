package com.quickweather.dto.user;

import com.quickweather.dto.weatherDtos.weather.response.WeatherResponse;

import java.time.LocalDateTime;

public class UserSearchHistoryResponse {
    private String city;
    private LocalDateTime searchedAt;
    private WeatherResponse weather;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDateTime getSearchedAt() {
        return searchedAt;
    }

    public void setSearchedAt(LocalDateTime searchedAt) {
        this.searchedAt = searchedAt;
    }

    public WeatherResponse getWeather() {
        return weather;
    }

    public void setWeather(WeatherResponse weather) {
        this.weather = weather;
    }
}
