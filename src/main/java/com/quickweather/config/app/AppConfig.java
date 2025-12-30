package com.quickweather.config.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

/**
 * Main application configuration providing basic beans.
 * <p>
 * In this class:
 * <ul>
 *   <li>We define the {@link RestTemplate} client, used for making external HTTP calls.</li>
 *   <li>We provide a {@link Clock} for system time management, which makes time-related logic more testable.</li>
 * </ul>
 * These beans can be easily injected into different parts of the application.
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
