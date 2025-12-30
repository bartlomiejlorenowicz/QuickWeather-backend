package com.quickweather.dto.weatherDtos.forecast;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.quickweather.dto.weatherDtos.weather.request.Clouds;
import com.quickweather.dto.weatherDtos.weather.request.Main;
import com.quickweather.dto.weatherDtos.weather.request.Weather;
import com.quickweather.dto.weatherDtos.weather.request.Wind;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastItem {
    private long dt;
    private Main main;
    private List<Weather> weather;
    private Wind wind;
    private Clouds clouds;
    private String dt_txt;

    public long getDt() {
        return dt;
    }

    public void setDt(long dt) {
        this.dt = dt;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    public String getDt_txt() {
        return dt_txt;
    }

    public void setDt_txt(String dt_txt) {
        this.dt_txt = dt_txt;
    }
}
