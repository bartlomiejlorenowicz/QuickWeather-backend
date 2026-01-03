package com.quickweather.controller;

import com.quickweather.dto.user.UserSearchHistoryResponse;
import com.quickweather.dto.weatherDtos.weather.request.Main;
import com.quickweather.dto.weatherDtos.weather.response.WeatherResponse;
import com.quickweather.security.userdatails.CustomUserDetails;
import com.quickweather.service.weather.OpenWeatherServiceImpl;
import com.quickweather.service.weather.UserSearchHistoryService;
import com.quickweather.validation.IntegrationTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserHistoryControllerTest extends IntegrationTestConfig {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenWeatherServiceImpl currentWeatherService;

    private CustomUserDetails customUserDetails;

    @MockBean
    private UserSearchHistoryService userSearchHistoryService;

    @BeforeEach
    void setUp() {
        customUserDetails = new CustomUserDetails(
                1L,
                "bartek",
                "bartek",
                "bartek@wp.pl",
                "Pass123!",
                false,
                true,
                Set.of(),
                UUID.randomUUID()
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities())
        );
    }

    @Test
    void shouldReturnWeatherWithUserHistory() throws Exception {
        String city = "Warsaw";

        WeatherResponse response = new WeatherResponse();
        Main main = new Main();
        main.setTemp(25.0);
        response.setMain(main);
        response.setName(city);

        when(currentWeatherService.getWeatherAndSaveHistory(eq(city), any(CustomUserDetails.class)))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/history/current-with-user-history")
                        .param("city", city))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(city))
                .andExpect(jsonPath("$.main.temp").value(25.0));
    }

    @Test
    void shouldReturnUserSearchHistory() throws Exception {
        UserSearchHistoryResponse history1 = new UserSearchHistoryResponse();
        history1.setCity("Warsaw");
        UserSearchHistoryResponse history2 = new UserSearchHistoryResponse();
        history2.setCity("Kraków");
        List<UserSearchHistoryResponse> historyList = Arrays.asList(history1, history2);

        when(userSearchHistoryService.getUserSearchHistory(eq(customUserDetails.getUserId()), anyInt(), anyInt()))
                .thenReturn(historyList);

        mockMvc.perform(get("/api/v1/history")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].city").value("Warsaw"))
                .andExpect(jsonPath("$[1].city").value("Kraków"));
    }
}