package com.quickweather.validation.location;

import com.quickweather.dto.weatherDtos.weather.request.Coordinates;

import static java.util.Objects.isNull;

public abstract class CoordinatesValidator {
    private CoordinatesValidator next;

    public static CoordinatesValidator link(CoordinatesValidator first, CoordinatesValidator... chain) {
        CoordinatesValidator head = first;
        for (CoordinatesValidator nextInChain: chain) {
            head.next = nextInChain;
            head = nextInChain;
        }
        return first;
    }

    public abstract void validate(Coordinates coordinates);

    protected void validateNext(Coordinates coordinates) {
        if (isNull(next)) {
            return;
        }
        next.validate(coordinates);
    }
}
