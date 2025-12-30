package com.quickweather.domain.user;

import com.quickweather.admin.SecurityEventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_events")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SecurityEvent {

    public SecurityEvent(String username, SecurityEventType eventType, String ipAddress, LocalDateTime eventTime) {
        this.username = username;
        this.eventType = eventType;
        this.ipAddress = ipAddress;
        this.eventTime = eventTime;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    private SecurityEventType eventType;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "event_time")
    private LocalDateTime eventTime;
}
