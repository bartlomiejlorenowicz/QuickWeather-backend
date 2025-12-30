package com.quickweather.domain.weather;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class ApiQueryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city")
    private String city;

    @Column(name = "queried_at")
    private LocalDateTime queriedAt;

    public ApiQueryLog(String city, LocalDateTime queriedAt) {
        this.city = city;
        this.queriedAt = queriedAt;
    }

    public ApiQueryLog() {
    }
}
