package com.quickweather.repository;

import com.quickweather.domain.weather.ApiQueryLog;
import com.quickweather.dto.weatherDtos.weather.request.CityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiQueryLogRepository extends JpaRepository<ApiQueryLog, Long> {

    @Query("SELECT new com.quickweather.dto.weatherDtos.weather.request.CityLog(a.city, COUNT(a)) " +
            "FROM ApiQueryLog a " +
            "GROUP BY a.city " +
            "ORDER BY COUNT(a) DESC")
    Page<CityLog> findTopCityLogs(Pageable pageable);
}
