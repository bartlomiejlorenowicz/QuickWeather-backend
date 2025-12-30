package com.quickweather.domain.weather;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.quickweather.domain.user.User;
import com.quickweather.dto.WeatherApiResponse;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "user_search_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "city", nullable = false)
    private String city;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weather_api_response_id", referencedColumnName = "id", nullable = false)
    private WeatherApiResponse weatherApiResponse;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    @PrePersist
    protected void onCreate() {
        this.searchedAt = LocalDateTime.now();
    }

}
