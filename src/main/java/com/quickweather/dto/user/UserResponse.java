package com.quickweather.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UserResponse {

    private UUID uuid;
    private String firstName;
    private String lastName;
    private String email;
    private Set<String> roles;

    private boolean enabled;
    private boolean locked;

    private LocalDateTime createdAt;
}
