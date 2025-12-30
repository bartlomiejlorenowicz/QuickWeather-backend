package com.quickweather.domain.weather;

public enum ApiSource {
    OPEN_WEATHER("OPEN_WEATHER"),
    ACCU_WEATHER("ACCU_WEATHER");

    private final String displayName;

    ApiSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
