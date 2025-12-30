package com.quickweather.config.security;

import com.quickweather.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtFilter jwtFilter, CorsConfigurationSource corsConfigurationSource) {
        this.jwtFilter = jwtFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/v1/user/auth/login",
                                "/api/v1/user/register",
                                "/api/v1/user/auth/validate-reset-token",
                                "/api/v1/user/auth/reset-password",
                                "/api/v1/user/auth/set-new-password",
                                "/api/v1/weather/city",
                                "/api/v1/weather/forecast",
                                "/api/v1/weather/zipcode",
                                "/api/v1/weather/coordinate",
                                "/api/v1/weather/forecast/daily",
                                "/api/v1/user/auth/forgot-password",
                                "/api/v1/weather/air-quality",
                                "/api/v1/history",
                                "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/delete-user/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(
                                "/api/v1/admin/**"
                        ).hasRole("ADMIN")
                        .requestMatchers("/api/v1/history/current-with-user-history").authenticated()
                        .requestMatchers("/api/v1/user/auth/change-password").authenticated()
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint()));

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
