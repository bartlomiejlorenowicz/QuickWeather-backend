package com.quickweather.service.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickweather.domain.user.User;
import com.quickweather.domain.weather.ApiSource;
import com.quickweather.domain.weather.UserSearchHistory;
import com.quickweather.dto.apiResponse.OperationType;
import com.quickweather.dto.user.UserSearchHistoryResponse;
import com.quickweather.dto.weatherDtos.weather.response.WeatherResponse;
import com.quickweather.exceptions.UserNotFoundException;
import com.quickweather.repository.UserRepository;
import com.quickweather.repository.UserSearchHistoryRepository;
import com.quickweather.repository.WeatherApiResponseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSearchHistoryServiceTest {

    @Mock
    private UserSearchHistoryRepository userSearchHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WeatherApiResponseRepository weatherApiResponseRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserSearchHistoryService service;

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(
                UserNotFoundException.class,
                () -> service.saveSearchHistory(userId, "London", new WeatherResponse())
        );

        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void shouldSaveWeatherApiResponseWhenNotExists() {
        Long userId = 1L;
        String city = "London";

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(weatherApiResponseRepository.findByCityAndApiSource(city, ApiSource.OPEN_WEATHER))
                .thenReturn(null);

        when(weatherApiResponseRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        service.saveSearchHistory(userId, city, new WeatherResponse());

        verify(weatherApiResponseRepository).save(any());
        verify(userSearchHistoryRepository, never()).save(any());
    }

    @Test
    void shouldSaveUserSearchHistoryWhenWeatherApiResponseExists() {
        Long userId = 1L;
        String city = "London";

        User user = new User();
        user.setId(userId);

        OperationType.WeatherApiResponse apiResponse = new OperationType.WeatherApiResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(weatherApiResponseRepository.findByCityAndApiSource(city, ApiSource.OPEN_WEATHER))
                .thenReturn(apiResponse);

        service.saveSearchHistory(userId, city, new WeatherResponse());

        verify(userSearchHistoryRepository).save(any(UserSearchHistory.class));
    }

    @Test
    void shouldReturnMappedUserSearchHistory() throws Exception {
        Long userId = 1L;

        WeatherResponse weatherResponse = new WeatherResponse();
        OperationType.WeatherApiResponse apiResponse = new OperationType.WeatherApiResponse();
        apiResponse.setResponseJson(objectMapper.valueToTree(weatherResponse));

        UserSearchHistory history = new UserSearchHistory();
        history.setCity("London");
        history.setSearchedAt(LocalDateTime.now());
        history.setWeatherApiResponse(apiResponse);

        Page<UserSearchHistory> page = new PageImpl<>(List.of(history));

        when(userSearchHistoryRepository.findByUserId(eq(userId), any()))
                .thenReturn(page);

        when(objectMapper.treeToValue(any(), eq(WeatherResponse.class)))
                .thenReturn(weatherResponse);

        List<UserSearchHistoryResponse> result =
                service.getUserSearchHistory(userId, 0, 10);

        assertEquals(1, result.size());
        assertEquals("London", result.get(0).getCity());
        assertNotNull(result.get(0).getWeather());
    }

    @Test
    void shouldThrowRuntimeExceptionWhenJsonDeserializationFails() throws Exception {
        Long userId = 1L;

        OperationType.WeatherApiResponse apiResponse = new OperationType.WeatherApiResponse();
        apiResponse.setResponseJson(objectMapper.createObjectNode());

        UserSearchHistory history = new UserSearchHistory();
        history.setWeatherApiResponse(apiResponse);

        Page<UserSearchHistory> page = new PageImpl<>(List.of(history));

        when(userSearchHistoryRepository.findByUserId(eq(userId), any()))
                .thenReturn(page);

        when(objectMapper.treeToValue(any(), eq(WeatherResponse.class)))
                .thenThrow(new JsonProcessingException("boom") {});

        assertThrows(RuntimeException.class,
                () -> service.getUserSearchHistory(userId, 0, 10));
    }
}
