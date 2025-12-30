package com.quickweather.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "user_activity_log")
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "email")
    private String email;

    private String city;

    private LocalDateTime timestamp;

    private String activity;

    public UserActivityLog() {}

    public UserActivityLog(String userId, String email, String city, LocalDateTime timestamp, String activity) {
        this.userId = userId;
        this.email = email;
        this.city = city;
        this.timestamp = timestamp;
        this.activity = activity;
    }
}
