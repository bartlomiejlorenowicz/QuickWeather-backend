package com.quickweather.schedule;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickweather.domain.weather.ApiSource;
import com.quickweather.dto.WeatherApiResponse;
import com.quickweather.dto.WeatherApiResponseHistory;
import com.quickweather.repository.WeatherApiResponseHistoryRepository;
import com.quickweather.repository.WeatherApiResponseRepository;
import com.quickweather.validation.IntegrationTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
@Transactional
public class WeatherCleanupServiceIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private WeatherApiResponseRepository weatherApiResponseRepository;

    @Autowired
    private WeatherApiResponseHistoryRepository weatherApiResponseHistoryRepository;

    @Autowired
    private WeatherCleanupService weatherCleanupService;

    @Autowired
    private Clock clock;

    @BeforeEach
    void clearDatabase() {
        weatherApiResponseRepository.deleteAll();
        weatherApiResponseHistoryRepository.deleteAll();
    }


    @Test
    void testArchiveOldWeatherData_WithDatabase() {
        LocalDateTime now = LocalDateTime.now(clock);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseJson = objectMapper.createObjectNode().put("key", "value");
        JsonNode requestJson = objectMapper.createObjectNode().put("query", "example");

        WeatherApiResponse oldData = new WeatherApiResponse();
        oldData.setCity("London");
        oldData.setApiSource(ApiSource.OPEN_WEATHER);
        oldData.setCreatedAt(now.minusHours(6));
        oldData.setResponseJson(responseJson);
        oldData.setRequestJson(requestJson);

        WeatherApiResponse recentData = new WeatherApiResponse();
        recentData.setCity("Warsaw");
        recentData.setApiSource(ApiSource.OPEN_WEATHER);
        recentData.setCreatedAt(now.minusHours(4));
        recentData.setResponseJson(responseJson);
        recentData.setRequestJson(requestJson);

        weatherApiResponseRepository.save(oldData);
        weatherApiResponseRepository.save(recentData);

        weatherCleanupService.archiveOldWeatherData();

        List<WeatherApiResponseHistory> all = weatherApiResponseHistoryRepository.findAll();
        assertEquals(1, all.size());
        assertEquals("London", all.get(0).getCity());

        List<WeatherApiResponse> currentData = weatherApiResponseRepository.findAll();
        assertEquals(1, currentData.size());
        assertEquals("Warsaw", currentData.get(0).getCity());
    }


}
