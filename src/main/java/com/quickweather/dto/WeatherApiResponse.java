package com.quickweather.dto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.quickweather.domain.weather.ApiSource;
import com.quickweather.domain.weather.UserSearchHistory;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "weather_api_responses")
public class WeatherApiResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city")
    private String city;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApiSource apiSource;

    @Type(JsonBinaryType.class)
    @Column(name = "response_json", columnDefinition = "json")
    private JsonNode responseJson;

    @Type(JsonBinaryType.class)
    @Column(name = "request_json", columnDefinition = "json")
    private JsonNode requestJson;

    @JsonManagedReference
    @OneToMany(mappedBy = "weatherApiResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSearchHistory> userSearchHistories = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
