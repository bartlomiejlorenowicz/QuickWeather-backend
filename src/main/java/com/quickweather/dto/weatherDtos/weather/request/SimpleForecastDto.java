package com.quickweather.dto.weatherDtos.weather.request;

public class SimpleForecastDto {
    private String date;
    private double temperature;
    private double windSpeed;

    public SimpleForecastDto(String date, double temperature, double windSpeed) {
        this.date = date;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
    }

    // Gettery i settery
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }
}
