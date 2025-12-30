package com.quickweather.controller.user;

import com.quickweather.dto.user.UserSearchHistoryResponse;
import com.quickweather.dto.weatherDtos.weather.response.WeatherResponse;
import com.quickweather.security.userdatails.CustomUserDetails;
import com.quickweather.service.weather.OpenWeatherServiceImpl;
import com.quickweather.service.weather.UserSearchHistoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/history")
@Validated
public class UserHistoryController {

    private final UserSearchHistoryService userSearchHistoryService;
    private final OpenWeatherServiceImpl currentWeatherService;

    public UserHistoryController(UserSearchHistoryService userSearchHistoryService, OpenWeatherServiceImpl currentWeatherService) {
        this.userSearchHistoryService = userSearchHistoryService;
        this.currentWeatherService = currentWeatherService;
    }

    @GetMapping("/current-with-user-history")
    public WeatherResponse getWeatherWithHistory(@RequestParam String city,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        return currentWeatherService.getWeatherAndSaveHistory(city, userDetails);
    }

    @GetMapping
    public List<UserSearchHistoryResponse> getUserSearchHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return userSearchHistoryService.getUserSearchHistory(userDetails.getUserId(), page, size);
    }
}
