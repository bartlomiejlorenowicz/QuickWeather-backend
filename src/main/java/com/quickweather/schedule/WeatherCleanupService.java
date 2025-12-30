package com.quickweather.schedule;

import com.quickweather.dto.apiResponse.WeatherApiResponseHistory;
import com.quickweather.dto.apiResponse.OperationType;
import com.quickweather.repository.WeatherApiResponseHistoryRepository;
import com.quickweather.repository.WeatherApiResponseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@EnableScheduling
public class WeatherCleanupService {
    private final WeatherApiResponseRepository weatherApiResponseRepository;
    private final WeatherApiResponseHistoryRepository weatherApiResponseHistoryRepository;
    private final Clock clock;

    public WeatherCleanupService(WeatherApiResponseRepository weatherApiResponseRepository, WeatherApiResponseHistoryRepository weatherApiResponseHistoryRepository, Clock clock) {
        this.weatherApiResponseRepository = weatherApiResponseRepository;
        this.weatherApiResponseHistoryRepository = weatherApiResponseHistoryRepository;
        this.clock = clock;
    }

    @Transactional
    @Scheduled(cron = "${weather.cleanup.cron}")
    public void archiveOldWeatherData() {
        LocalDateTime expiryTime = LocalDateTime.now(clock).minusHours(5);

        List<OperationType.WeatherApiResponse> oldData = weatherApiResponseRepository.findAllByCreatedAtBefore(expiryTime);

        if (oldData == null || oldData.isEmpty()) {
            log.info("No records to archive {}", LocalDateTime.now(clock));
            return;
        }

        log.info("Found {} records to archive.", oldData.size());

        try {
            List<WeatherApiResponseHistory> historyData = oldData.stream()
                    .map(this::mapToHistory)
                    .toList();

            weatherApiResponseHistoryRepository.saveAll(historyData);
            weatherApiResponseRepository.deleteAll(oldData);
            log.info("Successfully archived {} records.", oldData.size());
        } catch (Exception e) {
            log.error("Archiving process failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    private WeatherApiResponseHistory mapToHistory(OperationType.WeatherApiResponse record) {
        WeatherApiResponseHistory history = new WeatherApiResponseHistory();
        history.setCity(record.getCity());
        history.setCountryCode(record.getCountryCode());
        history.setApiSource(record.getApiSource());
        history.setResponseJson(record.getResponseJson());
        history.setRequestJson(record.getRequestJson());
        history.setCreatedAt(record.getCreatedAt());
        history.setArchivedAt(LocalDateTime.now(clock));
        return history;
    }
}
