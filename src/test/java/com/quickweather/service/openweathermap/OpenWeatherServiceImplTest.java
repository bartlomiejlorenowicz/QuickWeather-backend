package com.quickweather.service.openweathermap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickweather.dto.weatherDtos.forecast.HourlyForecastResponseDto;
import com.quickweather.dto.weatherDtos.weather.response.WeatherByZipCodeResponseDto;
import com.quickweather.dto.weatherDtos.weather.response.WeatherResponse;
import com.quickweather.domain.weather.ApiSource;
import com.quickweather.dto.WeatherApiResponse;
import com.quickweather.dto.weatherDtos.weather.response.WeatherResponseData;
import com.quickweather.exceptions.WeatherErrorType;
import com.quickweather.exceptions.WeatherServiceException;
import com.quickweather.repository.WeatherApiResponseRepository;
import com.quickweather.service.weather.OpenWeatherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenWeatherServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private WeatherApiResponseRepository weatherApiResponseRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OpenWeatherServiceImpl currentWeatherService;

    private static final String CITY = "London";
    private static final String ZIPCODE = "37-203";
    private static final String COUNTRY_CODE = "pl";
    private static final String TEST_API_KEY = "test-api-key";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/";

    @BeforeEach
    void setUp(){
        ReflectionTestUtils.setField(currentWeatherService, "apiKey", TEST_API_KEY);
        ReflectionTestUtils.setField(currentWeatherService, "apiUrl", API_URL);
    }

    @Test
    void testGetCacheWeatherResponseWithOpenWeatherServiceImpl() {

        WeatherApiResponse mockResponse = new WeatherApiResponse();
        mockResponse.setCity("London");
        mockResponse.setApiSource(ApiSource.OPEN_WEATHER);
        mockResponse.setCreatedAt(LocalDateTime.now());

        when(weatherApiResponseRepository.findTopByCityAndApiSourceOrderByCreatedAtDesc("London", ApiSource.OPEN_WEATHER))
                .thenReturn(Optional.of(mockResponse));

        Optional<WeatherApiResponse> result = currentWeatherService.getCacheWeatherResponse("London", ApiSource.OPEN_WEATHER);

        assertTrue(result.isPresent());
        assertEquals("London", result.get().getCity());
        Mockito.verify(weatherApiResponseRepository).findTopByCityAndApiSourceOrderByCreatedAtDesc("London", ApiSource.OPEN_WEATHER);
    }

    @Test
    void shouldSaveWeatherResponseWithCorrectValues() throws Exception {

        String city = "London";
        String countryCode = "GB";
        String responseJson = "{\"temp\": 20}";
        String requestJson = "{\"query\": \"London\"}";

        JsonNode responseJsonNode = objectMapper.readTree(responseJson);
        JsonNode requestJsonNode = objectMapper.readTree(requestJson);

        WeatherApiResponse mockSavedResponse = new WeatherApiResponse();
        mockSavedResponse.setCity(city);
        mockSavedResponse.setCountryCode(countryCode);
        mockSavedResponse.setApiSource(ApiSource.OPEN_WEATHER);
        mockSavedResponse.setResponseJson(responseJsonNode);
        mockSavedResponse.setRequestJson(requestJsonNode);
        mockSavedResponse.setCreatedAt(LocalDateTime.now());

        Mockito.when(weatherApiResponseRepository.save(any(WeatherApiResponse.class))).thenReturn(mockSavedResponse);

        WeatherResponseData data = new WeatherResponseData(city, countryCode, ApiSource.OPEN_WEATHER, responseJson, requestJson);
        currentWeatherService.saveWeatherResponse(data);

        ArgumentCaptor<WeatherApiResponse> captor = ArgumentCaptor.forClass(WeatherApiResponse.class);
        Mockito.verify(weatherApiResponseRepository).save(captor.capture());
        WeatherApiResponse capturedResponse = captor.getValue();

        assertEquals(city, capturedResponse.getCity());
        assertEquals(countryCode, capturedResponse.getCountryCode());
        assertEquals(ApiSource.OPEN_WEATHER, capturedResponse.getApiSource());
        assertEquals(responseJsonNode, capturedResponse.getResponseJson());
        assertEquals(requestJsonNode, capturedResponse.getRequestJson());
    }

    @Test
    void testGetCurrentWeather() throws JsonProcessingException {
        // Mockowanie pustego cache
        OpenWeatherServiceImpl spyService = Mockito.spy(currentWeatherService);

        Mockito.doReturn(Optional.empty())
                .when(spyService).getCacheWeatherResponse(CITY, ApiSource.OPEN_WEATHER);


        WeatherResponse mockWeatherResponse = new WeatherResponse();
        mockWeatherResponse.setName(CITY);

        when(restTemplate.getForObject(any(URI.class), Mockito.eq(WeatherResponse.class)))
                .thenReturn(mockWeatherResponse);

        when(objectMapper.writeValueAsString(mockWeatherResponse))
                .thenReturn("{\"name\": \"London\"}");

        // Mockowanie requestJson
        when(objectMapper.writeValueAsString(any(Map.class)))
                .thenReturn("{\"q\": \"London\", \"appid\": \"test-api-key\", \"units\": \"metric\"}");

        WeatherResponse result = spyService.getCurrentWeatherByCity(CITY);

        assertEquals(mockWeatherResponse, result);
        Mockito.verify(restTemplate).getForObject(any(URI.class), Mockito.eq(WeatherResponse.class));
        Mockito.verify(objectMapper, Mockito.atLeastOnce()).writeValueAsString(any());
    }

    @Test
    void testGetCurrentWeatherFromCache() throws JsonProcessingException {
        OpenWeatherServiceImpl spyService = Mockito.spy(currentWeatherService);

        Mockito.doReturn(Optional.empty())
                .when(spyService).getCacheWeatherResponse(CITY, ApiSource.OPEN_WEATHER);

        WeatherResponse mockWeatherResponse = new WeatherResponse();
        mockWeatherResponse.setName(CITY);

        when(restTemplate.getForObject(any(URI.class), Mockito.eq(WeatherResponse.class)))
                .thenReturn(mockWeatherResponse);

        when(objectMapper.writeValueAsString(mockWeatherResponse))
                .thenReturn("{\"name\": \"London\"}");

        // Mockowanie requestJson
        when(objectMapper.writeValueAsString(any(Map.class)))
                .thenReturn("{\"q\": \"London\", \"appid\": \"test-api-key\", \"units\": \"metric\"}");

        WeatherResponse result = spyService.getCurrentWeatherByCity(CITY);

        assertEquals(mockWeatherResponse, result);
        Mockito.verify(restTemplate).getForObject(any(URI.class), Mockito.eq(WeatherResponse.class));
    }


    @Test
    void testGetCurrentWeatherHttpClientErrorException() {

        when(restTemplate.getForObject(any(URI.class), eq(WeatherResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        WeatherServiceException exception = assertThrows(WeatherServiceException.class, () -> currentWeatherService.getCurrentWeatherByCity(CITY));

        assertEquals("Data not found for: " + CITY, exception.getMessage());
    }

    @Test
    void testGetCurrentWeatherGeneralException() {

        when(restTemplate.getForObject(any(URI.class), eq(WeatherResponse.class)))
                .thenThrow(new RuntimeException("General exception"));

        WeatherServiceException exception = assertThrows(WeatherServiceException.class, () -> {
            currentWeatherService.getCurrentWeatherByCity(CITY);
        });

        assertEquals("An unknown error occurred while fetching weather data for: " + CITY, exception.getMessage());
    }

    @Test
    void testGetCurrentWeatherByZipcodeShouldReturnCorrect() {

        WeatherByZipCodeResponseDto mockWeather = new WeatherByZipCodeResponseDto();

        when(restTemplate.getForObject(any(URI.class), eq(WeatherByZipCodeResponseDto.class)))
                .thenReturn(mockWeather);

        WeatherByZipCodeResponseDto result = currentWeatherService.getCurrentWeatherByZipcode(ZIPCODE, COUNTRY_CODE);

        assertEquals(mockWeather, result);
    }

    @Test
    void testGetCurrentWeatherByZipcodeGeneratedUri() {

        currentWeatherService.getCurrentWeatherByZipcode(ZIPCODE, COUNTRY_CODE);

        Mockito.verify(restTemplate).getForObject(Mockito.argThat(argument -> {
            URI uri = argument;
            return uri.toString().contains("zip=" + ZIPCODE + "," + COUNTRY_CODE)
                    && uri.toString().contains("appid=" + TEST_API_KEY)
                    && uri.toString().contains("lang=en");
        }), eq(WeatherByZipCodeResponseDto.class));
    }

    @Test
    void testHttpClientErrorNotFoundExceptionForZipcode() {

        when(restTemplate.getForObject(any(URI.class), eq(WeatherByZipCodeResponseDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        RuntimeException exception = assertThrows(WeatherServiceException.class, () -> currentWeatherService.getCurrentWeatherByZipcode(ZIPCODE, COUNTRY_CODE));

        assertEquals("Data not found for: " + ZIPCODE + "," + COUNTRY_CODE, exception.getMessage());
    }

    @Test
    void testGeneralExceptionForZipcode() {

        when(restTemplate.getForObject(any(URI.class), eq(WeatherByZipCodeResponseDto.class)))
                .thenThrow(new WeatherServiceException(WeatherErrorType.UNKNOWN_ERROR, "General error"));

        RuntimeException exception = assertThrows(WeatherServiceException.class, () -> currentWeatherService.getCurrentWeatherByZipcode(ZIPCODE, COUNTRY_CODE));

        assertEquals("An unknown error occurred while fetching weather data for: " + ZIPCODE + "," + COUNTRY_CODE, exception.getMessage());
    }

    @Test
    void testGet5DaysForecastEvery3HoursShouldReturnCorrect() {

        HourlyForecastResponseDto mockForecast = new HourlyForecastResponseDto();

        when(restTemplate.getForObject(any(URI.class), eq(HourlyForecastResponseDto.class)))
                .thenReturn(mockForecast);

        HourlyForecastResponseDto result = currentWeatherService.get5DaysForecastEvery3Hours(CITY);

        assertEquals(mockForecast, result);
    }

    @Test
    void testGet5DaysForecastEvery3HoursHttpClientErrorException() {

        when(restTemplate.getForObject(any(URI.class), eq(HourlyForecastResponseDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        WeatherServiceException exception = assertThrows(WeatherServiceException.class, () -> currentWeatherService.get5DaysForecastEvery3Hours(CITY));

        assertEquals("Data not found for: " + CITY, exception.getMessage());
    }

    @Test
    void testGet5DaysForecastEvery3HoursGeneralErrorException() {

        when(restTemplate.getForObject(any(URI.class), eq(HourlyForecastResponseDto.class)))
                .thenThrow(new RuntimeException("General error"));

        WeatherServiceException exception = assertThrows(WeatherServiceException.class, () -> currentWeatherService.get5DaysForecastEvery3Hours(CITY));

        assertEquals("An unknown error occurred while fetching weather data for: " + CITY, exception.getMessage());
    }


}