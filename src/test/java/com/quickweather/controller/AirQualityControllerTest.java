package com.quickweather.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import com.quickweather.domain.user.User;
import com.quickweather.validation.IntegrationTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WireMockTest(httpPort = 8081)
@SpringBootTest
@ExtendWith(CommonTestSetupExtension.class)
class AirQualityControllerTest extends IntegrationTestConfig {

    private String tokenUser;
    private User testUser;

    @RegisterExtension
    CommonTestSetupExtension setup = new CommonTestSetupExtension();

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        tokenUser = setup.getTokenUser();
        testUser = setup.getTestUser();
    }

    @Value("${open.weather.api.key}")
    private String apiKey;

    private final String url = "/api/v1/weather";

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

}