package com.quickweather.repository;

import com.quickweather.domain.weather.ApiSource;
import com.quickweather.dto.apiResponse.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherApiResponseRepository extends JpaRepository<OperationType.WeatherApiResponse, Long> {

    Optional<OperationType.WeatherApiResponse> findTopByCityAndApiSourceOrderByCreatedAtDesc(String city, ApiSource apiSource);

    List<OperationType.WeatherApiResponse> findAllByCreatedAtBefore(LocalDateTime expiryTime);

    OperationType.WeatherApiResponse findByCityAndApiSource(String city, ApiSource openWeather);
}
