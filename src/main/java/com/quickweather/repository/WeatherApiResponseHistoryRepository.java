package com.quickweather.repository;

import com.quickweather.domain.weather.ApiSource;
import com.quickweather.dto.apiResponse.WeatherApiResponseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface WeatherApiResponseHistoryRepository extends JpaRepository<WeatherApiResponseHistory, Long> {
    Optional<WeatherApiResponseHistory> findFirstByCityAndApiSourceOrderByArchivedAtDesc(String city, ApiSource apiSource);
}
