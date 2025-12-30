package com.quickweather.service.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickweather.domain.weather.ApiSource;
import com.quickweather.dto.WeatherApiResponse;
import com.quickweather.dto.weatherDtos.weather.response.WeatherResponseData;
import com.quickweather.exceptions.WeatherErrorType;
import com.quickweather.exceptions.WeatherServiceException;
import com.quickweather.repository.WeatherApiResponseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public abstract class WeatherServiceBase {

    protected RestTemplate restTemplate;
    protected final WeatherApiResponseRepository weatherApiResponseRepository;
    protected final ObjectMapper objectMapper;

    protected WeatherServiceBase(RestTemplate restTemplate, WeatherApiResponseRepository weatherApiResponseRepository, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.weatherApiResponseRepository = weatherApiResponseRepository;
        this.objectMapper = objectMapper;
    }

    protected <T> T fetchWeatherData(URI url, Class<T> responseType, String identifier) {
        try {
            return restTemplate.getForObject(url, responseType);
        } catch (HttpClientErrorException e) {
            handleHttpClientError(e, identifier);
        } catch (Exception e) {
            log.error("An unknown error occurred while fetching weather data for {}: {}", identifier, e.getMessage());
            throw new WeatherServiceException(WeatherErrorType.UNKNOWN_ERROR, "An unknown error occurred while fetching weather data for: " + identifier);
        }
        throw new UnsupportedOperationException("Fetching weather data is unsupported.");
    }

    protected void handleHttpClientError(HttpClientErrorException e, String identifier) {
        log.error("HTTP error fetching weather data for {}: {}", identifier, e.getMessage());

        int statusCode = e.getStatusCode().value();

        switch (statusCode) {
            case 401:
                throw new WeatherServiceException(WeatherErrorType.INVALID_API_KEY, "Invalid API key");
            case 404:
                throw new WeatherServiceException(WeatherErrorType.DATA_NOT_FOUND, "Data not found for: " + identifier);
            case 503:
                throw new WeatherServiceException(WeatherErrorType.WEATHER_DATA_UNAVAILABLE, "Weather service unavailable");
            default:
                throw new WeatherServiceException(WeatherErrorType.EXTERNAL_API_ERROR, "Error fetching weather data for: " + identifier);
        }
    }

    //pobiera dane z bazy jesli sa dostepne
    public Optional<WeatherApiResponse> getCacheWeatherResponse(String city, ApiSource apiSource) {
        return weatherApiResponseRepository.findTopByCityAndApiSourceOrderByCreatedAtDesc(city, apiSource);
    }

    //zapisuje JSON do bazy
    public void saveWeatherResponse(WeatherResponseData data) throws JsonProcessingException {

        JsonNode validatedResponseJson = validateJson(data.getResponseJson(), "Response", data.getCity());
        JsonNode validatedRequestJson = validateJson(data.getRequestJson(), "Request", data.getCity());

        WeatherApiResponse weatherApiResponse = new WeatherApiResponse();
        weatherApiResponse.setCity(data.getCity());
        weatherApiResponse.setCountryCode(data.getCountryCode());
        weatherApiResponse.setApiSource(data.getApiSource());
        weatherApiResponse.setRequestJson(validatedRequestJson);
        weatherApiResponse.setResponseJson(validatedResponseJson);
        weatherApiResponse.setCreatedAt(LocalDateTime.now());

        weatherApiResponseRepository.save(weatherApiResponse);
    }

//    ============ Helper Method ===============

    private JsonNode validateJson(String json, String type, String city) throws JsonProcessingException {
        if (json == null || json.isEmpty()) {
            log.error("Response JSON is null or empty for city: {}", type, city);
            throw new WeatherServiceException(WeatherErrorType.DATA_NOT_FOUND,
                    type + " Response JSON is invalid for city: " + city);
        }
        return objectMapper.readTree(json);
    }

}
