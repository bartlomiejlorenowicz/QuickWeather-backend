package com.quickweather.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.quickweather.domain.weather.ApiSource;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "weather_api_responses_history")
public class WeatherApiResponseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city")
    private String city;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "api_source", nullable = false)
    private ApiSource apiSource;

    @Type(JsonBinaryType.class)
    @Column(name = "response_json", columnDefinition = "json")
    private JsonNode responseJson;

    @Type(JsonBinaryType.class)
    @Column(name = "request_json", columnDefinition = "json")
    private JsonNode requestJson;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "archived_at", nullable = false)
    private LocalDateTime archivedAt;
}
