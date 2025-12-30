package com.quickweather.schedule;

import com.quickweather.domain.weather.ApiSource;
import com.quickweather.dto.WeatherApiResponse;
import com.quickweather.dto.WeatherApiResponseHistory;
import com.quickweather.repository.WeatherApiResponseHistoryRepository;
import com.quickweather.repository.WeatherApiResponseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherCleanupServiceTest {

    @Mock
    private WeatherApiResponseRepository weatherApiResponseRepository;

    @Mock
    private WeatherApiResponseHistoryRepository weatherApiResponseHistoryRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private WeatherCleanupService weatherCleanupService;

    // Check that only old data is archived and deleted.
    @Test
    void testArchiveOldWeatherData_RemovesOnlyOldData() {
        Instant fixedInstant = Instant.parse("2024-01-01T12:00:00Z");
        ZoneId zoneId = ZoneId.systemDefault();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(zoneId);

        LocalDateTime now = LocalDateTime.now(clock);
        WeatherApiResponse oldRecord = new WeatherApiResponse();
        oldRecord.setCity("London");
        oldRecord.setCreatedAt(now.minusHours(6)); // older than 5 hours

        WeatherApiResponse recentRecord = new WeatherApiResponse();
        recentRecord.setCity("Warsaw");
        recentRecord.setCreatedAt(now.minusHours(4)); // younger than 5 hours

        when(weatherApiResponseRepository.findAllByCreatedAtBefore(any()))
                .thenReturn(List.of(oldRecord));

        weatherCleanupService.archiveOldWeatherData();

        ArgumentCaptor<List<WeatherApiResponseHistory>> captor = ArgumentCaptor.forClass(List.class);
        verify(weatherApiResponseHistoryRepository).saveAll(captor.capture());
        List<WeatherApiResponseHistory> archivedData = captor.getValue();

        assertEquals(1, archivedData.size());
        assertEquals("London", archivedData.get(0).getCity());

        verify(weatherApiResponseRepository).deleteAll(List.of(oldRecord));

        verify(weatherApiResponseRepository, never()).deleteAll(List.of(recentRecord));
    }

    // Checking whether data is correctly transferred to history.
    @Test
    void testArchiveOldWeatherData_DataArchivedAndDeleted() {
        Instant fixedInstant = Instant.parse("2024-01-01T12:00:00Z");
        ZoneId zoneId = ZoneId.systemDefault();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(zoneId);

        LocalDateTime now = LocalDateTime.now(clock);
        WeatherApiResponse response = new WeatherApiResponse();
        response.setCity("New York");
        response.setCountryCode("US");
        response.setApiSource(ApiSource.OPEN_WEATHER);
        response.setResponseJson(null);
        response.setRequestJson(null);
        response.setCreatedAt(now.minusHours(6));

        List<WeatherApiResponse> mockData = List.of(response);

        when(weatherApiResponseRepository.findAllByCreatedAtBefore(any())).thenReturn(mockData);

        weatherCleanupService.archiveOldWeatherData();

        ArgumentCaptor<List<WeatherApiResponseHistory>> captor = ArgumentCaptor.forClass(List.class);
        verify(weatherApiResponseHistoryRepository).saveAll(captor.capture());
        List<WeatherApiResponseHistory> savedHistory = captor.getValue();

        assertEquals(1, savedHistory.size());
        assertEquals("New York", savedHistory.get(0).getCity());
        assertEquals("US", savedHistory.get(0).getCountryCode());
        assertEquals(response.getApiSource(), savedHistory.get(0).getApiSource());
        assertEquals(response.getCreatedAt(), savedHistory.get(0).getCreatedAt());

        verify(weatherApiResponseRepository).deleteAll(mockData);
    }
}