package com.quickweather.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.quickweather.domain.user.Role;
import com.quickweather.domain.user.User;
import com.quickweather.repository.RoleRepository;
import com.quickweather.repository.UserRepository;
import com.quickweather.security.JwtTestUtil;
import com.quickweather.security.TestConfig;
import com.quickweather.service.user.UserRoleService;
import com.quickweather.service.weather.OpenWeatherServiceImpl;
import com.quickweather.validation.IntegrationTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WireMockTest(httpPort = 8081)
@Import(TestConfig.class)
@SpringBootTest
class WeatherControllerTest extends IntegrationTestConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private OpenWeatherServiceImpl openWeatherServiceImpl;

    private String tokenUser;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Set<Role> roles = new java.util.HashSet<>();
        userRoleService.assignDefaultUserRole(roles);

        User user = User.builder()
                .firstName("Adam")
                .lastName("Nowak")
                .email("testUser@wp.pl")
                .password(passwordEncoder.encode("testPassword"))
                .isEnabled(true)
                .roles(roles)
                .build();

        userRepository.save(user);

        tokenUser = jwtTestUtil.generateToken(user.getEmail(), "ROLE_USER");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Value("${open.weather.api.key}")
    private String apiKey;

    private final String url = "/api/v1/weather";

    @Test
    void shouldReturnWeatherData_WhenCityIsValid() throws Exception {
        String responseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/app/responses/current_weather.json")));

        stubFor(WireMock.get(urlPathEqualTo("/data/2.5/weather"))
                .withQueryParam("q", equalTo("London"))
                .withQueryParam("appid", equalTo(apiKey))
                .withQueryParam("units", equalTo("metric"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                        .withStatus(200)));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/city")
                        .header("Authorization", "Bearer " + tokenUser)
                        .param("city", "London")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.main.temp").value(2.48))
                .andExpect(jsonPath("$.weather[0].description").value("overcast clouds"))
                .andExpect(jsonPath("$.name").value("London"));
    }

    @Test
    void shouldReturnBadRequest_WhenCityIsBlank() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url + "/city")
                        .header("Authorization", "Bearer " + tokenUser)
                        .param("city", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("City name cannot be blank"));
    }

    @Test
    void shouldReturnNotFound_WhenCityIsUnknown() throws Exception {
        stubFor(WireMock.get(urlPathEqualTo("/data/2.5/weather"))
                .withQueryParam("q", equalTo("UnknownCity"))
                .withQueryParam("appid", equalTo(apiKey))
                .withQueryParam("units", equalTo("metric"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("{\"message\": \"city not found\"}")));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/city")
                        .header("Authorization", "Bearer " + tokenUser)
                        .param("city", "UnknownCity")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Data not found for: Unknowncity"));
    }

    @Test
    void shouldReturnWeatherData_WhenZipcodeIsValid() throws Exception {
        String responseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/app/responses/current_weather_by_zipcode.json")));

        stubFor(WireMock.get(urlPathEqualTo("/data/2.5/weather"))
                .withQueryParam("zip", equalTo("37-203,pl"))
                .willReturn(aResponse()
                        .withBody(responseBody)
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/zipcode")
                        .param("zipcode", "37-203")
                        .param("countryCode", "pl")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.main.pressure").value(1023))
                .andExpect(jsonPath("$.weather[0].description").value("overcast clouds"));
    }

    @Test
    void shouldReturnBadRequest_WhenCountryCodeIsInvalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url + "/zipcode")
                        .param("zipcode", "37-203")
                        .param("countryCode", "1111")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Country code must be 2 letters"));
    }

    @Test
    void shouldReturnBadRequest_WhenZipcodeIsMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url + "/zipcode")
                        .header("Authorization", "Bearer " + tokenUser)
                        .param("countryCode", "pl")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required request parameter 'zipcode' is missing"));
    }

    @Test
    void shouldReturn5DaysForecast_WhenCityIsValid() throws Exception {
        String weatherResponse = new String(Files.readAllBytes(Paths.get("src/test/resources/app/responses/forecast_for_5_days.json")));

        stubFor(WireMock.get(urlPathEqualTo("/data/2.5/forecast"))
                .withQueryParam("q", equalTo("London"))
                .withQueryParam("appid", equalTo(apiKey))
                .withQueryParam("units", equalTo("metric"))
                .withQueryParam("lang", equalTo("pl"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(weatherResponse)
                        .withStatus(200)));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/forecast")
                        .header("Authorization", "Bearer " + tokenUser)
                        .param("city", "London")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2024-11-04 12:00:00"))
                .andExpect(jsonPath("$[0].temperature").value(15.0))
                .andExpect(jsonPath("$[0].windSpeed").value(3.5));
    }

    @Test
    void shouldReturnAirPollutionData_WhenCityValid() throws Exception {
        String weatherResponse = new String(Files.readAllBytes(Paths.get("src/test/resources/app/responses/current_weather.json")));
        stubFor(WireMock.get(urlPathEqualTo("/data/2.5/weather"))
                .withQueryParam("q", equalTo("London"))
                .withQueryParam("appid", equalTo(apiKey))
                .withQueryParam("units", equalTo("metric"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(weatherResponse)
                        .withStatus(200)));

        String responseDto = new String(Files.readAllBytes(Paths.get("src/test/resources/app/responses/air_pollution_response.json")));
        stubFor(WireMock.get(urlPathEqualTo("/data/2.5/air_pollution"))
                .withQueryParam("lat", equalTo("51.5085"))
                .withQueryParam("lon", equalTo("-0.1257"))
                .withQueryParam("appid", equalTo(apiKey))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseDto)
                        .withStatus(200)));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/air-quality")
                        .header("Authorization", "Bearer " + tokenUser)
                        .param("city", "London")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list[0].main.aqi").value(2.0));
    }

    @Test
    void shouldReturnForecastByCityAndDays_WhenParametersValid() throws Exception {
        String responseDto = new String(Files.readAllBytes(Paths.get("src/test/resources/app/responses/forecast_by_city_and_by_days.json")));

        stubFor(WireMock.get(urlPathEqualTo("/data/2.5/forecast"))
                .withQueryParam("q", equalTo("Warsaw"))
                .withQueryParam("cnt", equalTo("3"))
                .withQueryParam("units", equalTo("metric"))
                .withQueryParam("appid", equalTo(apiKey))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseDto)
                        .withStatus(200)));

        mockMvc.perform(MockMvcRequestBuilders.get(url + "/forecast/daily")
                        .header("Authorization", "Bearer " + tokenUser)
                        .param("city", "Warsaw")
                        .param("cnt", "3")
                        .param("units", "metric")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list[0].main.temp").value(-2.34))
                .andExpect(jsonPath("$.list[1].weather[0].main").value("Clouds"))
                .andExpect(jsonPath("$.list[2].wind.speed").value(2.66));
    }

    @Test
    void shouldReturnWeatherByZipcode_whenParametersValid() throws Exception {
        String responseJson = new String(Files.readAllBytes(Paths.get("src/test/resources/app/responses/current_weather_by_zipcode.json")));

        stubFor(WireMock.get(urlPathEqualTo("/data/2.5/weather"))
                .withQueryParam("zip", equalTo("37-203,pl"))
                .withQueryParam("appid", equalTo(apiKey))
                .withQueryParam("lang", equalTo("en"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson)
                        .withStatus(200)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/weather/zipcode")
                        .param("zipcode", "37-203")
                        .param("countryCode", "pl")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.main.temp").value(-2.43))
                .andExpect(jsonPath("$.weather[0].main").value("Clouds"))
                .andExpect(jsonPath("$.weather[0].description").value("overcast clouds"))
                .andExpect(jsonPath("$.wind.speed").value(2.75))
                .andExpect(jsonPath("$.visibility").value(10000));
    }

    @Test
    void shouldReturnWeatherResponse_whenParametersValid() throws Exception {
        String responseJson = new String(Files.readAllBytes(Paths.get("src/test/resources/app/responses/current_weather.json")));

        stubFor(WireMock.get(urlPathEqualTo("/data/2.5/weather"))
                .withQueryParam("lat", equalTo("51.5085"))
                .withQueryParam("lon", equalTo("-0.1257"))
                .withQueryParam("appid", equalTo(apiKey))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson)
                        .withStatus(200)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/weather/coordinate")
                .param("lat", "51.5085")
                .param("lon", "-0.1257")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.main.temp").value(2.48))
                .andExpect(jsonPath("$.weather[0].main").value("Clouds"))
                .andExpect(jsonPath("$.visibility").value(10000));


    }

    @Test
    void shouldReturnBadRequest_WhenLongitudeIsOutOfBounds() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url + "/coordinate")
                        .param("lat", "51.5085")
                        .param("lon", "-181")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Longitude must be between -180 and 180"));

    }

    @Test
    void shouldReturnBadRequest_WhenLongitudeIsEmpty() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url + "/coordinate")
                        .param("lat", "-91")
                        .param("lon", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("coordinate Longitude is empty"));
    }

    @Test
    void shouldReturnBadRequest_WhenLatitudeIsOutOfBounds() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url + "/coordinate")
                        .param("lat", "-91")
                        .param("lon", "50.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Latitude must be between -90 and 90"));
    }

}